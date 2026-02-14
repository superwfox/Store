package sudark2.Sudark.store.NPC;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import sudark2.Sudark.store.Data.UniqueStoreData;
import sudark2.Sudark.store.Store;
import sudark2.Sudark.store.Util.MethodUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class NPCManager {

    private static final ConcurrentHashMap<String, ServerPlayer> npcEntities = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, String> entityIdToKey = new ConcurrentHashMap<>();
    private static EntityDataAccessor<Byte> SKIN_PARTS;
    private static EntityDataAccessor<Byte> SHARED_FLAGS;

    static {
        try {
            Field f = net.minecraft.world.entity.player.Player.class.getDeclaredField("f_36089_");
            f.setAccessible(true);
            SKIN_PARTS = (EntityDataAccessor<Byte>) f.get(null);
            Field sf = net.minecraft.world.entity.Entity.class.getDeclaredField("f_19805_");
            sf.setAccessible(true);
            SHARED_FLAGS = (EntityDataAccessor<Byte>) sf.get(null);
        } catch (Exception ignored) {}
    }

    public static ServerPlayer spawn(String name, Location loc) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel level = ((CraftWorld) loc.getWorld()).getHandle();
        String npcKey = MethodUtil.getLocCode(loc);

        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        String[] skin = UniqueStoreData.getSkin(npcKey);
        if (skin != null) {
            profile.getProperties().put("textures", new Property("textures", skin[0], skin[1]));
        }

        ServerPlayer npc = new ServerPlayer(server, level, profile);
        injectFakeConnection(npc, server);
        npc.forceSetPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        if (SKIN_PARTS != null) npc.m_20088_().m_135381_(SKIN_PARTS, (byte) 0x7F);
        npc.m_20242_(true);
        npc.getBukkitEntity().setInvulnerable(true);

        level.m_7967_(npc);
        npcEntities.put(npcKey, npc);
        entityIdToKey.put(npc.m_19879_(), npcKey);
        broadcastSpawn(npc);
        return npc;
    }

    private static void injectFakeConnection(ServerPlayer npc, MinecraftServer server) {
        Connection fakeConn = new Connection(PacketFlow.CLIENTBOUND);
        fakeConn.f_129468_ = new EmbeddedChannel(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {}
        });
        npc.f_8906_ = new ServerGamePacketListenerImpl(server, fakeConn, npc);
    }

    public static void remove(String npcKey) {
        ServerPlayer npc = npcEntities.remove(npcKey);
        if (npc == null) return;
        entityIdToKey.remove(npc.m_19879_());
        npc.m_142687_(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        broadcastRemove(npc);
    }

    public static void removeAll() {
        for (ServerPlayer npc : npcEntities.values()) {
            npc.m_142687_(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            broadcastRemove(npc);
        }
        npcEntities.clear();
        entityIdToKey.clear();
    }

    public static void respawnAll(Map<String, String> npcMapping) {
        for (var entry : npcMapping.entrySet()) {
            Location loc = parseNpcKeyToLocation(entry.getValue());
            if (loc != null) spawn(entry.getKey(), loc);
        }
    }

    public static void applySkin(String npcKey, String value, String signature) {
        ServerPlayer npc = npcEntities.get(npcKey);
        if (npc == null) return;
        GameProfile profile = npc.m_36316_();
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", value, signature));
        UniqueStoreData.setSkin(npcKey, value, signature);
        broadcastRemove(npc);
        Bukkit.getScheduler().runTaskLater(Store.getInstance(), () -> broadcastSpawn(npc), 5L);
    }

    public static void setGlowing(String npcKey, boolean glow) {
        ServerPlayer npc = npcEntities.get(npcKey);
        if (npc == null || SHARED_FLAGS == null) return;
        byte flags = npc.m_20088_().m_135370_(SHARED_FLAGS);
        if (glow) flags = (byte) (flags | 0x40);
        else flags = (byte) (flags & ~0x40);
        npc.m_20088_().m_135381_(SHARED_FLAGS, flags);
        broadcastMetadata(npc);
    }

    private static void broadcastMetadata(ServerPlayer npc) {
        var metadata = npc.m_20088_().m_252804_();
        if (metadata == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            ServerGamePacketListenerImpl conn = getConn(p);
            if (conn != null)
                conn.m_9829_(new ClientboundSetEntityDataPacket(npc.m_19879_(), metadata));
        }
    }

    public static String getNpcKeyByEntityId(int entityId) {
        return entityIdToKey.get(entityId);
    }

    public static void showToPlayer(Player p) {
        ServerGamePacketListenerImpl conn = getConn(p);
        if (conn == null) return;
        for (ServerPlayer npc : npcEntities.values()) sendSpawnPackets(conn, npc);
    }

    public static ConcurrentHashMap<String, ServerPlayer> getNpcEntities() {
        return npcEntities;
    }

    static Location parseNpcKeyToLocation(String npcKey) {
        String[] parts = npcKey.split("_");
        if (parts.length != 4) return null;
        try {
            var world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            return new Location(world,
                    Integer.parseInt(parts[1]) + 0.5,
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]) + 0.5);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static ServerGamePacketListenerImpl getConn(Player p) {
        return ((CraftPlayer) p).getHandle().f_8906_;
    }

    private static void broadcastSpawn(ServerPlayer npc) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            ServerGamePacketListenerImpl conn = getConn(p);
            if (conn != null) sendSpawnPackets(conn, npc);
        }
    }

    private static void broadcastRemove(ServerPlayer npc) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            ServerGamePacketListenerImpl conn = getConn(p);
            if (conn == null) continue;
            conn.m_9829_(new ClientboundRemoveEntitiesPacket(npc.m_19879_()));
            conn.m_9829_(new ClientboundPlayerInfoRemovePacket(List.of(npc.m_20148_())));
        }
    }

    private static int lookTaskId = -1;

    public static void startLookTask() {
        if (lookTaskId != -1) return;
        lookTaskId = Bukkit.getScheduler().runTaskTimer(Store.getInstance(), () -> {
            for (ServerPlayer npc : npcEntities.values()) {
                Player nearest = findNearestPlayer(npc, 8);
                if (nearest == null) continue;
                Location npcLoc = npc.getBukkitEntity().getLocation();
                Location pLoc = nearest.getEyeLocation();
                double dx = pLoc.getX() - npcLoc.getX();
                double dy = pLoc.getY() - (npcLoc.getY() + 1.62);
                double dz = pLoc.getZ() - npcLoc.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                float pitch = (float) Math.toDegrees(-Math.atan2(dy, dist));
                byte yawByte = (byte) (yaw * 256f / 360f);
                byte pitchByte = (byte) (pitch * 256f / 360f);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    ServerGamePacketListenerImpl conn = getConn(p);
                    if (conn == null) continue;
                    conn.m_9829_(new ClientboundRotateHeadPacket(npc, yawByte));
                    conn.m_9829_(new ClientboundMoveEntityPacket.Rot(
                            npc.m_19879_(), yawByte, pitchByte, true));
                }
            }
        }, 5L, 5L).getTaskId();
    }

    public static void stopLookTask() {
        if (lookTaskId != -1) {
            Bukkit.getScheduler().cancelTask(lookTaskId);
            lookTaskId = -1;
        }
    }

    private static Player findNearestPlayer(ServerPlayer npc, double maxDist) {
        Location loc = npc.getBukkitEntity().getLocation();
        Player nearest = null;
        double nearestDist = maxDist;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().equals(loc.getWorld())) continue;
            double d = p.getLocation().distance(loc);
            if (d < nearestDist) {
                nearestDist = d;
                nearest = p;
            }
        }
        return nearest;
    }

    private static void sendSpawnPackets(ServerGamePacketListenerImpl conn, ServerPlayer npc) {
        conn.m_9829_(new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc));
        conn.m_9829_(new ClientboundAddPlayerPacket(npc));
        var metadata = npc.m_20088_().m_252804_();
        if (metadata != null)
            conn.m_9829_(new ClientboundSetEntityDataPacket(npc.m_19879_(), metadata));
        Bukkit.getScheduler().runTaskLater(Store.getInstance(), () ->
                conn.m_9829_(new ClientboundPlayerInfoRemovePacket(List.of(npc.m_20148_()))), 40L);
    }
}
