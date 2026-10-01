package com.tunombre.friendesnpcs.listeners;

import com.tunombre.friendesnpcs.FriendesNPCs;
import com.tunombre.friendesnpcs.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PacketListener implements Listener {

    private final FriendesNPCs plugin;

    public PacketListener(FriendesNPCs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (NPC npc : plugin.getNpcManager().getAllNPCs()) {
            plugin.getNpcManager().spawnForPlayer(npc, player);
        }
    }
}