package com.tunombre.friendesnpcs;

import com.tunombre.friendesnpcs.commands.CommandManager;
import com.tunombre.friendesnpcs.holograms.HologramManager;
import com.tunombre.friendesnpcs.listeners.PacketListener;
import com.tunombre.friendesnpcs.npc.NPCManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FriendesNPCs extends JavaPlugin {

    private static FriendesNPCs instance;
    private NPCManager npcManager;
    private HologramManager hologramManager;
    private boolean skinsRestorerEnabled;

    @Override
    public void onEnable() {
        instance = this;

        skinsRestorerEnabled = getServer().getPluginManager().getPlugin("SkinsRestorer") != null;
        if (skinsRestorerEnabled) {
            getLogger().info("SkinsRestorer detectado - soporte de skins activado.");
        } else {
            getLogger().info("SkinsRestorer no encontrado - usando skins por defecto.");
        }

        saveDefaultConfig();

        this.npcManager = new NPCManager(this);
        this.hologramManager = new HologramManager(this);

        CommandManager commandManager = new CommandManager(this);
        getCommand("fnpc").setExecutor(commandManager);
        getCommand("fnpc").setTabCompleter(commandManager);

        getServer().getPluginManager().registerEvents(new PacketListener(this), this);

        getLogger().info("FriendesNPCs activado correctamente.");
    }

    @Override
    public void onDisable() {
        if (npcManager != null) npcManager.saveAll();
        if (hologramManager != null) hologramManager.saveAll();
        getLogger().info("FriendesNPCs desactivado.");
    }

    public static FriendesNPCs getInstance() {
        return instance;
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public boolean isSkinsRestorerEnabled() {
        return skinsRestorerEnabled;
    }
}