# XaeroSync

Velocity 代理端插件，解决 Xaero 小地图/世界地图在群组服跨服时的数据丢失问题。

## 原理

Xaero 地图模组使用 `serverId` 区分不同子服的地图数据。玩家切换子服后，模组如果没收到新的 `serverId`，会把不同子服的地图混在一起。

本插件通过 Xaero 的插件通道持续向客户端发送当前子服的 `serverId`，确保模组始终知道玩家在哪个子服，从而正确隔离每个子服的地图数据。

## 特性

- 每 2 秒心跳广播，防止进地狱/末地后 `serverId` 丢失
- 玩家跨服瞬间立即补发，无需等待心跳周期
- `serverId` 基于子服名称的 `hashCode` 计算，稳定一致

## 安装

1. 将 JAR 放入 Velocity 的 `plugins/` 目录
2. 重启 Velocity 或执行 `velocity plug load xaerosync`

## 要求

- Velocity 3.4+
- Java 17+
- 客户端安装 Xaero's Minimap / Xaero's World Map