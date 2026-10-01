package com.tunombre.friendesnpcs.npc;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;

import java.util.*;

public class NPC {

    private final UUID uuid;
    private String name;
    private Location location;
    private ServerPlayer serverPlayer;
    private String skinPlayerName;
    private final List<String> hologramLines = new ArrayList<>();
    private final List<NpcAction> actions = new ArrayList<>();
    private int cooldownSeconds = 0;
    private long lastClickTime = 0;

    public NPC(UUID uuid, String name, Location location, ServerPlayer serverPlayer, String skinPlayerName) {
        this.uuid = uuid;
        this.name = name;
        this.location = location;
        this.serverPlayer = serverPlayer;
        this.skinPlayerName = skinPlayerName;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) {
        this.location = location;
        this.serverPlayer.setPos(location.getX(), location.getY(), location.getZ());
    }
    public ServerPlayer getServerPlayer() { return serverPlayer; }
    public String getSkinPlayerName() { return skinPlayerName; }
    public void setSkinPlayerName(String skinPlayerName) { this.skinPlayerName = skinPlayerName; }
    public List<String> getHologramLines() { return hologramLines; }
    public List<NpcAction> getActions() { return actions; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }

    public boolean canClick() {
        if (cooldownSeconds <= 0) return true;
        return System.currentTimeMillis() - lastClickTime > cooldownSeconds * 1000L;
    }

    public void updateClickTime() {
        this.lastClickTime = System.currentTimeMillis();
    }

    public static class NpcAction {
        private final ActionType type;
        private final String value;

        public NpcAction(ActionType type, String value) {
            this.type = type;
            this.value = value;
        }

        public ActionType getType() { return type; }
        public String getValue() { return value; }
    }

    public enum ActionType {
        MESSAGE, COMMAND, SERVER
    }
}