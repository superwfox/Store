# Store - 经验等级商店插件

基于 Spigot 1.20.1 的服务器商店系统，使用玩家经验等级作为交易货币。
- 支持玩家自由上架物品、全局官方商店和NPC特殊商店。
- 针对 Mohist 1.20.1 完全适配。

## 主要功能

### 玩家商店
- 任意玩家可通过 `/store` 打开商店界面
- 点击向日葵图标进入出售流程，将物品放入界面后关闭，按提示输入价格和备注
- 商品直接展示实际物品（首个物品），显示卖家名称、价格、发布时间和备注
- 点击商品查看详情，确认后扣除相应经验等级完成购买
- 买家获得物品，卖家下次上线时收到经验补偿
- **自己发布的商品带有精准采集附魔标识**
- **主人或OP可右键下架商品并返还物品**
- **详情页可右键撤回单个物品**

### 官方商店（全局）
- 通过 `/store official` 或快捷菜单打开
- 管理员使用 `/store update <价格> [备注]` 将手持物品添加到官方商店
- 左键购买商品，OP可右键删除商品
- 最大容量54格

### 特殊商店（NPC绑定）
- 管理员使用 `/store create <ID>` 在当前位置创建NPC商店
- 使用 `/store add <价格> [备注]` 将手持物品添加到最近的NPC商店
- 右键点击NPC打开商店界面，使用 `/store check <ID>` 通过命令打开
- OP可右键删除商品

### 快捷菜单
- 在 `world` 世界中蹲下并按 `F` 键（交换手持物品键）打开快捷菜单
- 点击金锭打开玩家商店，点击合金锭打开官方商店

### 交易系统
- 使用玩家经验等级作为货币
- **在线卖家实时收到经验，离线卖家上线时自动发放**
- 所有交易记录保存至 `Records.csv` 便于审计

---

## 项目结构

```
src/main/java/sudark2/Sudark/store/
├── Store.java                       # 插件主类，注册命令和监听器
├── Command/
│   ├── StoreCommand.java            # 处理 /store 及其子命令
│   └── StoreTabCompleter.java       # Tab补全
├── Listener/
│   ├── PlayerStoreListener.java     # 玩家商店GUI交互
│   ├── OfficialStoreListener.java   # 官方商店GUI交互
│   ├── UniqueStoreListener.java     # 特殊商店GUI交互
│   ├── QuickMenuListener.java       # 快捷菜单交互 + F键触发
│   ├── EntityClickEvent.java        # 右键NPC触发商店
│   └── PlayerJoinListener.java      # 玩家登录时发放离线收益
├── Menu/
│   ├── PlayerStoreMenu.java         # 构建玩家商店/详情/出售界面
│   ├── OfficialStoreMenu.java       # 构建官方商店界面
│   ├── UniqueStoreMenu.java         # 构建特殊商店界面
│   └── QuickMenu.java               # 构建快捷选择菜单
├── Data/
│   ├── PlayerStoreData.java         # 玩家商品内存缓存
│   ├── OfficialStoreData.java       # 官方商店内存缓存
│   └── UniqueStoreData.java         # 特殊商店内存缓存 + NPC映射
├── File/
│   ├── FileManager.java             # 协调各Manager初始化和加载
│   ├── PlayerStoreManager.java      # 玩家商品的加载/保存
│   ├── OfficialStoreManager.java    # 官方商店的加载/保存
│   ├── UniqueStoreManager.java      # 特殊商店的加载/保存
│   ├── TransactionManager.java      # 交易记录与离线收益
│   └── SellManager.java             # 出售流程管理
├── Inventory/
│   └── ChatInput.java               # 聊天输入监听（价格/备注）
├── NPC/
│   └── InitNPC.java                 # 创建NPC并注册映射
└── Util/
    └── MethodUtil.java              # 工具方法：物品发放、购买处理、坐标编码
```

### 数据文件

```
plugins/Store/
├── data.yml              # 玩家商品元数据
├── npcList.yml           # NPC映射：商店ID → world_x_y_z
├── OfficialData.yml      # 官方商店商品元数据
├── payback.yml           # 离线卖家待发放经验
├── Records.csv           # 交易日志
├── items/                # 玩家商品序列化文件
├── officialStores/       # 特殊商店商品序列化文件
└── OfficialItems/        # 官方商店商品序列化文件
```

---

## 使用方法

### 玩家命令

| 命令 | 说明 |
|------|------|
| `/store` | 打开玩家商店 |
| `/store player` | 同上 |
| `/store official` | 打开官方商店 |

**快捷打开**：在 `world` 世界中蹲下按 `F` 键打开选择菜单

**出售物品流程**：
1. 执行 `/store` 打开玩家商店
2. 点击左上角的 **向日葵** 图标
3. 将要出售的物品放入界面，关闭界面
4. 在聊天栏输入 **价格**（纯数字）
5. 输入 **备注**（输入"无"表示无备注）
6. 商品上架成功

**购买物品**：
1. 在玩家商店点击商品查看详情
2. 点击右下角 **金锭** 确认购买
3. 经验等级足够则购买成功，物品存入背包

**下架/撤回**：
- 列表页右键自己的商品 → 下架整个商品，物品返还
- 详情页右键单个物品 → 撤回该物品，商品为空则自动下架

### 管理员命令（需OP）

| 命令 | 说明 |
|------|------|
| `/store create <ID>` | 在当前位置创建特殊商店NPC |
| `/store add <价格> [备注]` | 将手持物品添加到最近的NPC特殊商店 |
| `/store update <价格> [备注]` | 将手持物品添加到官方商店 |
| `/store check <ID>` | 打开指定ID的特殊商店 |
| `/store destroy <ID>` | 删除指定特殊商店及其数据 |
| `/store reload` | 重载插件数据并重建所有NPC |

### 依赖

- Spigot/Paper 1.20.1+
- [Citizens](https://www.spigotmc.org/resources/citizens.13811/) 插件（用于NPC创建）

---

## 颜色规范

| 代码 | 颜色 | 用途 |
|------|------|------|
| `§e` | 黄色 | 重点高亮 |
| `§b` | 青色 | 次要高亮 |
| `§f` | 白色 | 普通文本 |
| `§7` | 灰色 | 系统提示 |
