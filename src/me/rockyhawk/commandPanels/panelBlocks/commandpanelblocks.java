package me.rockyhawk.commandPanels.panelBlocks;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class commandpanelblocks implements CommandExecutor {
    commandpanels plugin;
    public commandpanelblocks(commandpanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        if (label.equalsIgnoreCase("cpb") || label.equalsIgnoreCase("commandpanelsblock") || label.equalsIgnoreCase("cpanelb")) {
            if(args.length == 2) {
                if (args[0].equalsIgnoreCase("add")) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Please execute command as a Player!"));
                        return true;
                    }
                    Player p = (Player)sender;
                    if(p.hasPermission("commandpanel.block.add")){
                        if(Objects.requireNonNull(plugin.config.getString("config.panel-blocks")).equalsIgnoreCase("false")){
                            sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Panel blocks disabled in config!"));
                            return true;
                        }
                        boolean foundPanel = false;
                        for(String[] temp : plugin.panelNames){
                            if(temp[0].equals(args[1])){
                                foundPanel = true;
                                break;
                            }
                        }
                        if(!foundPanel){
                            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.nopanel")));
                            return true;
                        }
                        Block blockType = p.getTargetBlock(null, 5);
                        if(blockType.getType() == Material.AIR){
                            sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Look at a block to add a panel!"));
                            return true;
                        }
                        Location blockLocation = blockType.getLocation();
                        String configValue = "blocks." + Objects.requireNonNull(blockLocation.getWorld()).getName() + "_" + blockLocation.getBlockX() + "_" + blockLocation.getBlockY() + "_" + blockLocation.getBlockZ() + ".panel";
                        plugin.blockConfig.set(configValue, args[1]);
                        try {
                            plugin.blockConfig.save(new File(plugin.getDataFolder() + File.separator + "blocks.yml"));
                        } catch (IOException e) {
                            plugin.debug(e);
                            sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Could not save to file!"));
                            return true;
                        }
                        //make the material name look okay
                        String materialNameFormatted = blockType.getType().toString().substring(0, 1).toUpperCase() + blockType.getType().toString().substring(1).toLowerCase();
                        materialNameFormatted = materialNameFormatted.replaceAll("_"," ");
                        sender.sendMessage(plugin.papi(tag + ChatColor.WHITE + args[1] + ChatColor.GREEN + " will now open when right clicking " + ChatColor.WHITE + materialNameFormatted));
                        return true;
                    }else{
                        sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
                        return true;
                    }
                }
            }
            if(args.length == 1){
                if (args[0].equalsIgnoreCase("remove")) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Please execute command as a Player!"));
                        return true;
                    }
                    Player p = (Player)sender;
                    if(p.hasPermission("commandpanel.block.remove")){
                        if(Objects.requireNonNull(plugin.config.getString("config.panel-blocks")).equalsIgnoreCase("false")){
                            sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Panel blocks disabled in config!"));
                            return true;
                        }
                        Block blockType = p.getTargetBlock(null, 5);
                        Location blockLocation = blockType.getLocation();
                        String configValue = "blocks." + Objects.requireNonNull(blockLocation.getWorld()).getName() + "_" + blockLocation.getBlockX() + "_" + blockLocation.getBlockY() + "_" + blockLocation.getBlockZ() + ".panel";
                        if(plugin.blockConfig.contains(configValue)){
                            plugin.blockConfig.set(configValue.replace(".panel",""), null);
                            try {
                                plugin.blockConfig.save(new File(plugin.getDataFolder() + File.separator + "blocks.yml"));
                            } catch (IOException e) {
                                plugin.debug(e);
                                sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Could not save to file!"));
                                return true;
                            }
                            sender.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Panel has been removed from block."));
                        }else{
                            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.nopanel")));
                        }
                        return true;
                    }else{
                        sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
                        return true;
                    }
                }
                if (args[0].equalsIgnoreCase("list")) {
                    if(sender.hasPermission("commandpanel.block.list")){
                        if(Objects.requireNonNull(plugin.config.getString("config.panel-blocks")).equalsIgnoreCase("false")){
                            sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Panel blocks disabled in config!"));
                            return true;
                        }
                        if(plugin.blockConfig.contains("blocks")){
                            if(Objects.requireNonNull(plugin.blockConfig.getConfigurationSection("blocks")).getKeys(false).size() == 0){
                                sender.sendMessage(plugin.papi(tag) + ChatColor.RED + "No panel blocks found.");
                                return true;
                            }
                            sender.sendMessage(plugin.papi(tag) + ChatColor.DARK_AQUA + "Panel Block Locations:");
                            for (String location : Objects.requireNonNull(plugin.blockConfig.getConfigurationSection("blocks")).getKeys(false)) {
                                sender.sendMessage(ChatColor.GREEN + location.replaceAll("_"," ") + ": " + ChatColor.WHITE + plugin.blockConfig.getString("blocks." + location + ".panel"));
                            }
                        }else{
                            sender.sendMessage(plugin.papi(tag) + ChatColor.RED + "No panel blocks found.");
                        }
                        return true;
                    }else{
                        sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
                        return true;
                    }
                }
            }
        }
        plugin.helpMessage(sender);
        return true;
    }
}