package com.tunombre.friendesnpcs.holograms;

import com.tunombre.friendesnpcs.FriendesNPCs;
import com.tunombre.friendesnpcs.npc.NPC;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.*;

public class HologramManager {

    private final FriendesNPCs plugin;
    private final Map<UUID, List<TextDisplay>> hologramEntities = new HashMap<>();

    public HologramManager(FriendesNPCs plugin) {
        this.plugin = plugin;
    }

    public void updateHologram(NPC npc) {
        removeHologram(npc);

        List<String> lines = npc.getHologramLines();
        if (lines.isEmpty()) return;

        Location baseLoc = npc.getLocation().clone().add(0, 2.2, 0);
        List<TextDisplay> displays = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            Location lineLoc = baseLoc.clone().subtract(0, i * 0.3, 0);

            TextDisplay display = lineLoc.getWorld().spawn(lineLoc, TextDisplay.class, entity -> {
                entity.text(Component.text(lines.get(i)));
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                entity.setVisibleByDefault(false);
                entity.setPersistent(false);
            });

            displays.add(display);

            for (Player player : baseLoc.getWorld().getPlayers()) {
                player.showEntity(plugin, display);
            }
        }

        hologramEntities.put(npc.getUuid(), displays);
    }

    public void removeHologram(NPC npc) {
        List<TextDisplay> displays = hologramEntities.remove(npc.getUuid());
        if (displays != null) {
            for (TextDisplay display : displays) {
                display.remove();
            }
        }
    }

    public void addLine(NPC npc, String text) {
        npc.getHologramLines().add(text);
        updateHologram(npc);
    }

    public void removeLine(NPC npc, int index) {
        List<String> lines = npc.getHologramLines();
        if (index >= 0 && index < lines.size()) {
            lines.remove(index);
            updateHologram(npc);
        }
    }

    public void saveAll() {
        plugin.getLogger().info("Guardando hologramas...");
    }
}