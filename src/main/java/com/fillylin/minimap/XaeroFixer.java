package com.fillylin.minimap; // 记得改成你自己的包名

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit; // 引入时间单位

@Plugin(id = "xaerosync", name = "XaeroMapSync", version = "1.1", authors = {"FILLYLIN"})
public class XaeroFixer {

    private final ProxyServer server;
    private final Logger logger;

    private static final MinecraftChannelIdentifier MINIMAP_CHANNEL =
            MinecraftChannelIdentifier.create("xaerominimap", "main");
    private static final MinecraftChannelIdentifier WORLDMAP_CHANNEL =
            MinecraftChannelIdentifier.create("xaeroworldmap", "main");

    @Inject
    public XaeroFixer(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(MINIMAP_CHANNEL, WORLDMAP_CHANNEL);
        logger.info("Xaero跨服地图修复插件(心跳防掉版 v1.1) 已启动！");

        // 【新增核心：心跳广播任务】
        // 插件启动后，每隔 2 秒钟执行一次这个任务
        server.getScheduler().buildTask(this, () -> {
            // 遍历全服所有在线玩家
            for (Player player : server.getAllPlayers()) {
                player.getCurrentServer().ifPresent(serverConnection -> {
                    String serverName = serverConnection.getServerInfo().getName();
                    int serverId = serverName.hashCode();

                    byte[] payload = new byte[5];
                    payload[0] = 0;
                    payload[1] = (byte) (serverId >> 24);
                    payload[2] = (byte) (serverId >> 16);
                    payload[3] = (byte) (serverId >> 8);
                    payload[4] = (byte) serverId;

                    // 持续向玩家的小地图和大地图补发 ID，确保他们进地狱/末地也不会失忆
                    player.sendPluginMessage(MINIMAP_CHANNEL, payload);
                    player.sendPluginMessage(WORLDMAP_CHANNEL, payload);
                });
            }
        }).repeat(2, TimeUnit.SECONDS).schedule(); // 2秒钟循环一次
    }

    @Subscribe
    public void onPlayerJoinServer(ServerPostConnectEvent event) {
        // 保留玩家刚跨服瞬间的发包，保证跨服时能“秒切”地图，不用等那 2 秒钟
        event.getPlayer().getCurrentServer().ifPresent(serverConnection -> {
            String serverName = serverConnection.getServerInfo().getName();
            int serverId = serverName.hashCode();

            byte[] payload = new byte[5];
            payload[0] = 0;
            payload[1] = (byte) (serverId >> 24);
            payload[2] = (byte) (serverId >> 16);
            payload[3] = (byte) (serverId >> 8);
            payload[4] = (byte) serverId;

            event.getPlayer().sendPluginMessage(MINIMAP_CHANNEL, payload);
            event.getPlayer().sendPluginMessage(WORLDMAP_CHANNEL, payload);
        });
    }
}
