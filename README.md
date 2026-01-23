# Store - 经验等级商店插件

基于 Spigot 1.20.1 的服务器商店系统，使用玩家经验等级作为交易货币。支持玩家自由上架物品和管理员创建官方NPC商店。

## 主要功能

### 玩家商店
- 任意玩家可通过 `/store` 打开商店界面
- 点击向日葵图标进入出售流程，将物品放入界面后关闭，按提示输入价格和备注
- 商品以玩家头颅形式展示，显示卖家名称、价格、发布时间和备注
- 点击商品查看详情，确认后扣除相应经验等级完成购买
- 买家获得物品，卖家下次上线时收到经验补偿

### 官方商店
- 管理员使用 `/store create <ID>` 在当前位置创建NPC商店
- 使用 `/store add <价格> [备注]` 将手持物品添加到最近的NPC商店
- 右键点击NPC打开商店界面，左键购买商品
- OP可右键删除商品

### 交易系统
- 使用玩家经验等级作为货币
- 离线卖家的收益记录到 `payback.yml`，上线时自动发放
- 所有交易记录保存至 `Records.csv` 便于审计

---

## 项目结构

```
src/main/java/sudark2/Sudark/store/
├── Store.java                    # 插件主类，注册命令和监听器
├── Command/
│   ├── StoreCommand.java         # 处理 /store 及其子命令
│   └── StoreTabCompleter.java    # Tab补全
├── Listener/
│   ├── PlayerStoreListener.java  # 玩家商店GUI交互：出售/购买/详情
│   ├── OfficialStoreListener.java# 官方商店GUI交互：购买/删除
│   ├── EntityClickEvent.java     # 右键NPC触发商店
│   └── PlayerJoinListener.java   # 玩家登录时发放离线收益
├── Menu/
│   ├── PlayerStoreMenu.java      # 构建玩家商店/详情/出售界面
│   ├── OfficialStoreMenu.java    # 构建官方商店界面
│   └── SellManager.java          # 出售流程：等待价格输入→备注输入→存储
├── Data/
│   ├── PlayerStoreData.java      # 玩家商品内存缓存
│   └── OfficialStoreData.java    # 官方商店内存缓存 + NPC映射
├── File/
│   ├── FileManager.java          # 玩家商品的加载/保存
│   ├── OfficialStoreManager.java # 官方商店的加载/保存
│   └── TransactionManager.java   # 交易记录与离线收益
├── Inventory/
│   └── ChatInput.java            # 聊天输入监听（价格/备注）
├── NPC/
│   └── InitNPC.java              # 创建NPC并注册映射
└── Util/
    └── MethodUtil.java           # 坐标编码/寻找最近NPC
```

### 数据文件

```
plugins/Store/
├── data.yml              # 玩家商品元数据（卖家、价格、时间、物品文件名）
├── npcList.yml           # NPC映射：商店ID → world_x_y_z
├── payback.yml           # 离线卖家待发放经验
├── Records.csv           # 交易日志
├── items/                # 玩家商品序列化文件
│   └── player_<uuid>_<timestamp>.dat
└── officialStores/       # 官方商店商品序列化文件
    └── <world_x_y_z>.dat
```

---

## 使用方法

### 玩家命令

| 命令 | 说明 |
|------|------|
| `/store` | 打开玩家商店 |
| `/store player` | 同上 |
| `/store official <ID>` | 打开指定ID的官方商店 |

**出售物品流程**：
1. 执行 `/store` 打开玩家商店
2. 点击左上角的 **向日葵** 图标
3. 将要出售的物品放入界面，关闭界面
4. 在聊天栏输入 **价格**（纯数字）
5. 输入 **备注**（输入"无"表示无备注）
6. 商品上架成功

**购买物品**：
1. 在玩家商店点击商品头颅查看详情
2. 点击右下角 **金锭** 确认购买
3. 经验等级足够则购买成功，物品存入背包

### 管理员命令（需OP）

| 命令 | 说明 |
|------|------|
| `/store create <ID>` | 在当前位置创建名为ID的商店NPC |
| `/store add <价格> [备注]` | 将手持物品添加到最近的NPC商店 |
| `/store destroy <ID>` | 删除指定商店及其数据 |
| `/store reload` | 重载插件数据并重建所有NPC |

**创建官方商店示例**：
```
# 站在目标位置执行
/store create 武器店

# 手持钻石剑
/store add 50 锋利V附魔
```

**删除商品**：  
OP在官方商店界面中 **右键** 商品即可删除。

### 依赖

- Spigot/Paper 1.20.1+
- [Citizens](https://www.spigotmc.org/resources/citizens.13811/) 插件（用于NPC创建）

### 构建

```bash
mvn clean package
```
生成的jar位于 `target/Store-1.0.jar`

---

## 颜色规范

| 代码 | 颜色 | 用途 |
|------|------|------|
| `§e` | 黄色 | 重点高亮 |
| `§b` | 青色 | 次要高亮 |
| `§f` | 白色 | 普通文本 |
| `§7` | 灰色 | 系统提示 |
