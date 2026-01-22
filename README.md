# Store - Mohist 1.20.1 商店插件

基于箱子GUI和NPC的经验等级交易系统，支持玩家商店和多个官方商店。

## 核心特性

### 玩家商店
- 玩家自由上架物品，使用玩家头颅展示
- 支持多物品打包出售
- 自动记录发布时间和备注信息
- 购买后自动移除商品

### 官方商店
- 基于NPC的多商店系统
- 每个NPC独立商店，独立库存
- 管理员通过命令添加商品到最近的NPC
- OP可右键删除商品

### 触发方式
- **玩家商店**: `/store` 或 `/store player`
- **官方商店**: 右键点击商店NPC 或 `/store official <商店ID>`

## 命令说明

### 玩家命令
```
/store              - 打开玩家商店
/store player       - 打开玩家商店
/store official <ID> - 打开指定官方商店
```

### 管理员命令
```
/store create <ID>  - 在当前位置创建商店NPC
/store add <价格> [备注] - 添加手持物品到最近的NPC商店
```

## 技术架构

### 性能优化设计

**事件驱动架构**
- 使用Bukkit事件系统响应玩家交互
- NPC点击事件直接触发商店打开
- 避免定时任务轮询，降低CPU占用

**ConcurrentHashMap缓存**
- 官方商店数据使用`ConcurrentHashMap<NPCKey, List<OfficialItem>>`缓存
- 线程安全，支持并发访问
- 内存中快速查询，减少IO操作

**分离式存储**
- 玩家商店：`data.yml` + `items/*.dat`
- 官方商店：`npcList.yml` + `officialStores/<npcKey>.dat`
- NPC映射：`NPCID -> NPCKey`，通过位置坐标生成唯一Key
- Base64序列化物品，避免YAML解析复杂对象

**职责分离**
- `Menu/`: GUI构建（PlayerStoreMenu, OfficialStoreMenu, SellManager）
- `Listener/`: 事件监听（PlayerStoreListener, OfficialStoreListener, EntityClickEvent）
- `Data/`: 数据模型（PlayerStoreData, OfficialStoreData）
- `File/`: 持久化（FileManager, OfficialStoreManager）

### 代码结构

```
Store.java                    # 主类
├─ Command/
│  ├─ StoreCommand.java       # 命令处理
│  └─ StoreTabCompleter.java  # Tab补全
├─ Listener/
│  ├─ PlayerStoreListener.java    # 玩家商店交互
│  ├─ OfficialStoreListener.java  # 官方商店交互
│  └─ EntityClickEvent.java       # NPC点击
├─ Menu/
│  ├─ PlayerStoreMenu.java    # 玩家商店GUI
│  ├─ OfficialStoreMenu.java  # 官方商店GUI
│  └─ SellManager.java         # 出售流程管理
├─ Data/
│  ├─ PlayerStoreData.java    # 玩家商店数据
│  └─ OfficialStoreData.java  # 官方商店数据
├─ File/
│  ├─ FileManager.java         # 玩家数据IO
│  └─ OfficialStoreManager.java # 官方商店IO
├─ Inventory/
│  └─ AnvilInput.java          # 铁砧输入
├─ NPC/
│  └─ InitNPC.java             # NPC初始化
└─ Util/
   └─ MethodUtil.java          # 工具方法
```

**直接调用模式**
- 所有类使用静态方法
- 避免对象实例化开销
- 减少内存占用和GC压力

### 数据持久化

**文件结构**
```
plugins/Store/
├─ data.yml              # 玩家商品元数据
├─ npcList.yml           # NPC映射 (NPCID: NPCKEY)
├─ items/                # 玩家物品序列化
│  └─ player_<uuid>_<timestamp>.dat
└─ officialStores/       # 官方商店数据
   └─ <npcKey>.dat
```

**npcList.yml格式**
```yaml
shop1: "100_64_200"
shop2: "150_64_250"
```

**NPCKey生成**
- 基于NPC位置坐标：`x_y_z`
- 确保每个位置唯一对应一个商店

## 颜色规范
- §e (黄色) - 重点高亮（上架成功）
- §b (青色) - 次要高亮
- §f (白色) - 普通文本
- §7 (灰色) - 系统提示
