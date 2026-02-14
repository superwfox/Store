# Store - 经验等级商店插件

Mohist 1.20.1 服务器商店系统，使用玩家经验等级作为交易货币。支持玩家自由上架、官方商店、NPC特殊商店、物品回收四种商店形态。

## NPC 系统

不依赖任何第三方NPC插件，直接通过 NMS `ServerPlayer` 实现假人NPC：

- 通过反射注入 `EmbeddedChannel` + 假 `Connection` + `ServerGamePacketListenerImpl`，使 `ServerPlayer` 脱离真实网络连接独立存在
- NPC 作为真实实体加入世界（`addFreshEntity`），支持原生 `PlayerInteractAtEntityEvent` 交互，无需额外碰撞检测
- 皮肤通过 Mojang API 异步获取，写入 `GameProfile` 的 textures 属性，反射设置 `DATA_PLAYER_MODE_CUSTOMISATION`（`f_36089_`）为 `0x7F` 启用全部7层皮肤
- 发光效果通过反射操作 `DATA_SHARED_FLAGS_ID`（`f_19805_`）的 `0x40` 位，手动广播 `ClientboundSetEntityDataPacket` 实现
- 朝向追踪：5tick 间隔的调度任务，计算最近玩家方位角，通过 `ClientboundRotateHeadPacket` + `ClientboundMoveEntityPacket.Rot` 广播
- 所有 SRG 混淆方法名适配 Mohist 1.20.1 的 `mohistdev` 映射

## 主要功能

### 玩家商店
- `/store` 打开商店，点击末地传送门框架进入出售流程
- 放入物品 → 关闭界面 → 聊天输入价格和备注 → 上架（3分钟超时自动返还）
- 自己的商品带精准采集附魔标识，主人或OP可右键下架，详情页可右键撤回单个物品
- 买家购买后卖家实时收到经验，离线卖家上线时自动补发

### 官方商店
- `/store official` 或快捷菜单打开
- `/store update <价格> [备注]` 添加商品，左键购买，OP右键删除

### 特殊商店（NPC绑定）
- `/store create <ID>` 在当前位置生成NPC商店
- `/store add <价格> [备注]` 添加商品到最近NPC（50格内，添加时NPC发光提示）
- `/store setskin <正版玩家ID>` 设置最近NPC的皮肤
- 右键NPC或 `/store check <ID>` 打开商店

### 快捷菜单
- 在 `world` 世界蹲下按 `F` 键打开，可快速进入三种商店

### 回收商店
- `/store recycle` 或快捷菜单打开
- `/store cycle <经验等级>` 设置可回收物品（物品数量为兑换单位）
- 放入物品关闭界面自动回收，不足或不匹配的物品返还

## 项目结构与性能分析

```
store/
├── Store.java                  # 插件入口，事件驱动注册
├── Command/
│   ├── StoreCommand.java       # /store 子命令分发
│   └── StoreTabCompleter.java  # Tab补全（职责分离）
├── Data/                       # 内存缓存层（ConcurrentHashMap）
│   ├── UniqueStoreData.java    # NPC商店 + NPC映射 + 皮肤映射
│   ├── OfficialStoreData.java  # 官方商店
│   ├── PlayerStoreData.java    # 玩家商店
│   └── RecycleStoreData.java   # 回收物品（Base64序列化键 → O(1)匹配）
├── File/                       # 持久化层
│   ├── FileManager.java        # 统一初始化/加载/保存入口
│   ├── UniqueStoreManager.java # NPC商店 + npcList.yml（含皮肤持久化）
│   ├── PlayerStoreManager.java # 玩家商品 BukkitObject 序列化
│   ├── OfficialStoreManager.java
│   ├── RecycleStoreManager.java
│   ├── TransactionManager.java # 交易记录CSV + 离线经验补发
│   └── SellManager.java        # 出售流程状态机（聊天输入）
├── Listener/                   # 事件驱动交互
│   ├── EntityClickEvent.java   # NPC右键 → entityId O(1)查表打开商店
│   ├── PlayerStoreListener.java
│   ├── OfficialStoreListener.java
│   ├── UniqueStoreListener.java
│   ├── RecycleStoreListener.java
│   ├── QuickMenuListener.java  # F键蹲下触发
│   └── PlayerJoinListener.java # 登录补发经验 + NPC可见性同步
├── Menu/                       # GUI构建（纯展示，无状态）
├── NPC/
│   ├── NPCManager.java         # NPC生命周期 + 数据包广播 + 朝向追踪
│   ├── InitNPC.java            # 创建NPC并注册映射
│   └── SkinFetcher.java        # Mojang API异步皮肤获取
├── Inventory/
│   └── ChatInput.java          # 聊天输入路由
└── Util/
    └── MethodUtil.java         # 购买/发放/坐标编码/NPC查找
```


### 性能设计要点

| 设计 | 说明 |
|------|------|
| ConcurrentHashMap 缓存 | Data 层全部使用 ConcurrentHashMap，NPC映射、皮肤映射、商品数据均为 O(1) 读写，异步操作线程安全 |
| entityId → npcKey 反查表 | `EntityClickEvent` 通过 `entityIdToKey` 直接 O(1) 定位NPC，无需遍历所有NPC实体 |
| 事件驱动而非轮询 | 商店交互、购买、回收全部基于 Bukkit 事件监听，不使用定时任务轮询状态 |
| 异步皮肤获取 | Mojang API 请求在异步线程执行，结果回调主线程应用，不阻塞服务器主循环 |
| 按需数据包广播 | NPC 的生成/移除/元数据变更通过手动构造数据包发送给在线玩家，避免依赖实体 tracker 的全量同步 |
| 回收物品 Base64 键匹配 | 将 ItemStack 序列化为 Base64 字符串作为 HashMap 键，O(1) 判断物品是否可回收，无需逐项比对 |
| 出售流程状态机 | SellManager 用 Set/Map 管理玩家出售状态，3分钟超时自动清理，避免内存泄漏 |

### 数据文件

```
plugins/Store/
├── data.yml              # 玩家商品元数据
├── npcList.yml           # NPC映射 + 皮肤持久化
├── OfficialData.yml      # 官方商店元数据
├── payback.yml           # 离线经验补发队列
├── Records.csv           # 交易审计日志
├── RecycleData.yml       # 回收物品元数据
├── items/                # 玩家商品序列化
├── OfficialItems/        # 官方商品序列化
├── RecycleItems/         # 回收物品序列化
└── UniqueStores/         # NPC商店商品序列化
```

## 命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/store` | 所有人 | 打开玩家商店 |
| `/store official` | 所有人 | 打开官方商店 |
| `/store recycle` | 所有人 | 打开回收商店 |
| `/store create <ID>` | OP | 创建NPC商店 |
| `/store destroy <ID>` | OP | 删除NPC商店 |
| `/store add <价格> [备注]` | OP | 添加商品到最近NPC商店 |
| `/store update <价格> [备注]` | OP | 添加商品到官方商店 |
| `/store check <ID>` | OP | 打开指定NPC商店 |
| `/store setskin <正版ID>` | OP | 设置最近NPC皮肤 |
| `/store cycle <经验等级>` | OP | 设置可回收物品 |
| `/store reload` | OP | 重载数据并重建NPC |

快捷方式：`world` 世界蹲下按 `F` 键打开选择菜单
