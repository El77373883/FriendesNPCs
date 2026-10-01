package com.tunombre.friendesnpcs.commands;

import com.tunombre.friendesnpcs.FriendesNPCs;
import com.tunombre.friendesnpcs.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final FriendesNPCs plugin;

    public CommandManager(FriendesNPCs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("friendesnpcs.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create": return handleCreate(sender, args);
            case "remove": return handleRemove(sender, args);
            case "list": return handleList(sender);
            case "rename": return handleRename(sender, args);
            case "skin": return handleSkin(sender, args);
            case "hologram": return handleHologram(sender, args);
            case "action": return handleAction(sender, args);
            case "info": return handleInfo(sender, args);
            case "reload":
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "Configuración recargada.");
                return true;
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo jugadores pueden crear NPCs.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /fnpc create <nombre> [skin_jugador]");
            return true;
        }

        Player player = (Player) sender;
        String name = args[1];
        String skin = args.length > 2 ? args[2] : null;

        NPC npc = plugin.getNpcManager().createNPC(name, player.getLocation(), skin);
        sender.sendMessage(ChatColor.GREEN + "NPC '" + name + "' creado con ID: " + npc.getUuid());
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /fnpc remove <nombre>");
            return true;
        }
        NPC npc = plugin.getNpcManager().getNPCByName(args[1]);
        if (npc == null) {
            sender.sendMessage(ChatColor.RED + "NPC no encontrado.");
            return true;
        }
        plugin.getNpcManager().removeNPC(npc.getUuid());
        sender.sendMessage(ChatColor.GREEN + "NPC eliminado.");
        return true;
    }

    private boolean handleList(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== NPCs registrados ===");
        for (NPC npc : plugin.getNpcManager().getAllNPCs()) {
            sender.sendMessage(ChatColor.YELLOW + "- " + npc.getName() + " (" + npc.getUuid() + ")");
        }
        return true;
    }

    private boolean handleRename(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /fnpc rename <id|nombre> <nuevo_nombre>");
            return true;
        }
        NPC npc = findNpc(args[1]);
        if (npc == null) {
            sender.sendMessage(ChatColor.RED + "NPC no encontrado.");
            return true;
        }
        npc.setName(args[2]);
        sender.sendMessage(ChatColor.GREEN + "NPC renombrado a: " + args[2]);
        return true;
    }

    private boolean handleSkin(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /fnpc skin <id|nombre> <jugador>");
            return true;
        }
        NPC npc = findNpc(args[1]);
        if (npc == null) {
            sender.sendMessage(ChatColor.RED + "NPC no encontrado.");
            return true;
        }
        npc.setSkinPlayerName(args[2]);
        sender.sendMessage(ChatColor.GREEN + "Skin actualizada a: " + args[2]);
        return true;
    }

    private boolean handleHologram(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /fnpc hologram <add|remove|clear> <id|nombre> [texto]");
            return true;
        }
        NPC npc = findNpc(args[2]);
        if (npc == null) {
            sender.sendMessage(ChatColor.RED + "NPC no encontrado.");
            return true;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "add":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Uso: /fnpc hologram add <id> <texto>");
                    return true;
                }
                plugin.getHologramManager().addLine(npc, String.join(" ", Arrays.copyOfRange(args, 3, args.length)));
                sender.sendMessage(ChatColor.GREEN + "Línea añadida al holograma.");
                break;
            case "remove":
                if (args.length < 4) return true;
                plugin.getHologramManager().removeLine(npc, Integer.parseInt(args[3]));
                sender.sendMessage(ChatColor.GREEN + "Línea eliminada.");
                break;
            case "clear":
                npc.getHologramLines().clear();
                plugin.getHologramManager().updateHologram(npc);
                sender.sendMessage(ChatColor.GREEN + "Holograma borrado.");
                break;
        }
        return true;
    }

    private boolean handleAction(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Uso: /fnpc action <add|clear> <id|nombre> <mensaje|comando>");
            return true;
        }
        NPC npc = findNpc(args[2]);
        if (npc == null) {
            sender.sendMessage(ChatColor.RED + "NPC no encontrado.");
            return true;
        }

        if (args[1].equalsIgnoreCase("clear")) {
            npc.getActions().clear();
            sender.sendMessage(ChatColor.GREEN + "Acciones eliminadas.");
            return true;
        }

        String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        npc.getActions().add(new NPC.NpcAction(NPC.ActionType.MESSAGE, value));
        sender.sendMessage(ChatColor.GREEN + "Acción añadida.");
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) return true;
        NPC npc = findNpc(args[1]);
        if (npc == null) {
            sender.sendMessage(ChatColor.RED + "NPC no encontrado.");
            return true;
        }
        sender.sendMessage(ChatColor.GOLD + "=== Info de " + npc.getName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "UUID: " + npc.getUuid());
        sender.sendMessage(ChatColor.YELLOW + "Ubicación: " + npc.getLocation());
        sender.sendMessage(ChatColor.YELLOW + "Skin: " + (npc.getSkinPlayerName() != null ? npc.getSkinPlayerName() : "Default"));
        sender.sendMessage(ChatColor.YELLOW + "Hologramas: " + npc.getHologramLines().size() + " líneas");
        sender.sendMessage(ChatColor.YELLOW + "Acciones: " + npc.getActions().size());
        return true;
    }

    private NPC findNpc(String input) {
        try {
            NPC npc = plugin.getNpcManager().getNPC(java.util.UUID.fromString(input));
            if (npc != null) return npc;
        } catch (IllegalArgumentException ignored) {}
        return plugin.getNpcManager().getNPCByName(input);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== FriendesNPCs ===");
        sender.sendMessage(ChatColor.YELLOW + "/fnpc create <nombre> [skin] - Crear NPC");
        sender.sendMessage(ChatColor.YELLOW + "/fnpc remove <id|nombre> - Eliminar NPC");
        sender.sendMessage(ChatColor.YELLOW + "/fnpc list - Listar NPCs");
        sender.sendMessage(ChatColor.YELLOW + "/fnpc rename <id> <nuevo> - Renombrar");
        sender.sendMessage(ChatColor.YELLOW + "/fnpc skin <id> <jugador> - Cambiar skin");
        sender.sendMessage(ChatColor.YELLOW + "/fnpc hologram <add|remove|clear> <id> [texto]");
        sender.sendMessage(ChatColor.YELLOW + "/fnpc action <add|clear> <id> <mensaje>");
        sender.sendMessage(ChatColor.YELLOW + "/fnpc info <id> - Información");
        sender.sendMessage(ChatColor.YELLOW + "/fnpc reload - Recargar config");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "remove", "list", "rename", "skin", "hologram", "action", "info", "reload"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rename")
                || args[0].equalsIgnoreCase("skin") || args[0].equalsIgnoreCase("info")) {
                plugin.getNpcManager().getAllNPCs().forEach(npc -> completions.add(npc.getName()));
            }
        }
        return completions;
    }
}