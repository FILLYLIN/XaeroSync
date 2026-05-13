package com.fillylin.minimap;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Plugin(id = "xaerosync", name = "XaeroMapSync", version = "1.1", authors = {"FILLYLIN"})
public class XaeroFixer {

    private final ProxyServer server;
    private final Logger logger;

    private static final MinecraftChannelIdentifier MINIMAP_CHANNEL =
            MinecraftChannelIdentifier.create("xaerominimap", "main");
    private static final MinecraftChannelIdentifier WORLDMAP_CHANNEL =
            MinecraftChannelIdentifier.create("xaeroworldmap", "main");

    private final Map<String, byte[]> payloadCache = new ConcurrentHashMap<>();

    @Inject
    public XaeroFixer(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(MINIMAP_CHANNEL, WORLDMAP_CHANNEL);
        logger.info("Xaero跨服地图修复插件(心跳防掉版 v1.1) 已启动!");

        server.getScheduler().buildTask(this, () -> {
            for (Player player : server.getAllPlayers()) {
                player.getCurrentServer().ifPresent(conn ->
                        sendPayload(player, conn.getServerInfo().getName()));
            }
        }).repeat(2, TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void onPlayerJoinServer(ServerPostConnectEvent event) {
        event.getPlayer().getCurrentServer().ifPresent(conn ->
                sendPayload(event.getPlayer(), conn.getServerInfo().getName()));
    }

    private void sendPayload(Player player, String serverName) {
        byte[] payload = payloadCache.computeIfAbsent(serverName, name -> {
            int id = name.hashCode();
            return new byte[] {
                    0,
                    (byte) (id >> 24),
                    (byte) (id >> 16),
                    (byte) (id >> 8),
                    (byte) id
            };
        });
        player.sendPluginMessage(MINIMAP_CHANNEL, payload);
        player.sendPluginMessage(WORLDMAP_CHANNEL, payload);
    }
}