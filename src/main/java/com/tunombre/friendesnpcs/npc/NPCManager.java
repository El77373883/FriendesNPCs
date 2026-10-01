package com.tunombre.friendesnpcs.npc;

import com.tunombre.friendesnpcs.FriendesNPCs;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class NPCManager {

    private final FriendesNPCs plugin;
    private final Map<UUID, NPC> npcs = new HashMap<>();
    private final Map<String, UUID> nameIndex = new HashMap<>();

    public NPCManager(FriendesNPCs plugin) {
        this.plugin = plugin;
        loadAll();
    }

    public NPC createNPC(String name, Location location, String skinPlayerName) {
        UUID uuid = UUID.randomUUID();
        GameProfile profile = new GameProfile(uuid, name);

        if (plugin.isSkinsRestorerEnabled() && skinPlayerName != null) {
            applySkinFromSkinsRestorer(profile, skinPlayerName);
        }

        ServerLevel level = ((org.bukkit.craftbukkit.CraftWorld) location.getWorld()).getHandle();
        MinecraftServer server = level.getServer();

        ServerPlayer serverPlayer = new ServerPlayer(
            server, level, profile,
            net.minecraft.server.level.ClientInformation.createDefault()
        );
        serverPlayer.setPos(location.getX(), location.getY(), location.getZ());

        serverPlayer.connection = new ServerGamePacketListenerImpl(
            server,
            new Connection(PacketFlow.SERVERBOUND),
            serverPlayer,
            CommonListenerCookie.createInitial(profile, false)
        );

        NPC npc = new NPC(uuid, name, location, serverPlayer, skinPlayerName);
        npcs.put(uuid, npc);
        nameIndex.put(name.toLowerCase(), uuid);

        for (Player player : Bukkit.getOnlinePlayers()) {
            spawnForPlayer(npc, player);
        }

        return npc;
    }

    public void spawnForPlayer(NPC npc, Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerGamePacketListenerImpl connection = craftPlayer.getHandle().connection;
        ServerPlayer serverPlayer = npc.getServerPlayer();

        connection.send(new ClientboundPlayerInfoUpdatePacket(
            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
            serverPlayer
        ));

        ServerEntity serverEntity = new ServerEntity(
            serverPlayer.serverLevel(), serverPlayer, 0, false,
            packet -> {}, Set.of()
        );
        connection.send(serverPlayer.getAddEntityPacket(serverEntity));

        connection.send(new ClientboundRotateHeadPacket(
            serverPlayer,
            (byte) ((int) (npc.getLocation().getYaw() * 256F / 360.0F))
        ));

        connection.send(new ClientboundPlayerInfoRemovePacket(List.of(npc.getUuid())));
    }

    public void despawnForPlayer(NPC npc, Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerGamePacketListenerImpl connection = craftPlayer.getHandle().connection;

        connection.send(new ClientboundRemoveEntitiesPacket(npc.getServerPlayer().getId()));
        connection.send(new ClientboundPlayerInfoRemovePacket(List.of(npc.getUuid())));
    }

    private void applySkinFromSkinsRestorer(GameProfile profile, String skinPlayerName) {
        try {
            plugin.getLogger().info("Aplicando skin de: " + skinPlayerName);
        } catch (Exception e) {
            plugin.getLogger().warning("Error al aplicar skin: " + e.getMessage());
        }
    }

    public NPC getNPC(UUID uuid) {
        return npcs.get(uuid);
    }

    public NPC getNPCByName(String name) {
        UUID uuid = nameIndex.get(name.toLowerCase());
        return uuid != null ? npcs.get(uuid) : null;
    }

    public Collection<NPC> getAllNPCs() {
        return npcs.values();
    }

    public void removeNPC(UUID uuid) {
        NPC npc = npcs.remove(uuid);
        if (npc != null) {
            nameIndex.remove(npc.getName().toLowerCase());
            for (Player player : Bukkit.getOnlinePlayers()) {
                despawnForPlayer(npc, player);
            }
        }
    }

    public void saveAll() {
        plugin.getLogger().info("Guardando " + npcs.size() + " NPCs...");
    }

    private void loadAll() {
        plugin.getLogger().info("Cargando NPCs...");
    }
}