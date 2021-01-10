package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class Commandpanel implements CommandExecutor {
    CommandPanels plugin;

    public Commandpanel(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //below is going to go through the files and find the right one
        Panel panel = null;
        if (args.length != 0) { //check to make sure the person hasn't just left it empty
            for(Panel tempPanel  : plugin.panelList){
                if(tempPanel.getName().equals(args[0])) {
                    panel = tempPanel;
                    break;
                }
            }
        }else{
            plugin.helpMessage(sender);
            return true;
        }
        if(panel == null){
            sender.sendMessage(plugin.papi(plugin.tag + plugin.config.getString("config.format.nopanel")));
            return true;
        }
        boolean disableCommand = false;
        if(panel.getConfig().contains("panelType")) {
            if (panel.getConfig().getStringList("panelType").contains("nocommand")) {
                //do not allow command with noCommand
                disableCommand =  true;
            }
        }
        //below will start the command, once it got the right file and panel
        if (cmd.getName().equalsIgnoreCase("cp") || cmd.getName().equalsIgnoreCase("commandpanel") || cmd.getName().equalsIgnoreCase("cpanel")) {
            if(!(sender instanceof Player)) {
                //do console command command
                if(args.length == 2){
                    if(!args[1].equals("item")){
                        if(plugin.openPanels.hasPanelOpen(plugin.getServer().getPlayer(args[1]).getName())) {
                            plugin.openPanels.skipPanels.add(plugin.getServer().getPlayer(args[1]).getName());
                        }
                        if(!disableCommand) {
                            plugin.openVoids.openCommandPanel(sender, plugin.getServer().getPlayer(args[1]), panel.getName(), panel.getConfig(), true);
                        }
                    }else{
                        sender.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Usage: /cp <panel> [item] [player]"));
                    }
                    return true;
                }else if(args.length == 3){
                    if (args[1].equals("item")) {
                        plugin.openVoids.giveHotbarItem(sender,plugin.getServer().getPlayer(args[2]),panel.getConfig(),true);
                    }else{
                        sender.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Usage: /cp <panel> item [player]"));
                    }
                    return true;
                } else {
                    sender.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Please execute command directed to a Player!"));
                    return true;
                }
            }else{
                //get player
                Player p = (Player) sender;
                //do player command
                if (args.length == 1) {
                    if(plugin.openPanels.hasPanelOpen(p.getName())) {
                        plugin.openPanels.skipPanels.add(p.getName());
                    }
                    if(!disableCommand) {
                        plugin.openVoids.openCommandPanel(sender, p, panel.getName(), panel.getConfig(), false);
                    }
                    return true;
                }else if(args.length == 2){
                    if (args[1].equals("item")) {
                        plugin.openVoids.giveHotbarItem(sender, p, panel.getConfig(), false);
                    }else{
                        if(plugin.openPanels.hasPanelOpen(plugin.getServer().getPlayer(args[1]).getName())) {
                            plugin.openPanels.skipPanels.add(plugin.getServer().getPlayer(args[1]).getName());
                        }
                        if(!disableCommand) {
                            plugin.openVoids.openCommandPanel(sender, plugin.getServer().getPlayer(args[1]), panel.getName(), panel.getConfig(), true);
                        }
                    }
                    return true;
                }else if(args.length == 3){
                    plugin.openVoids.giveHotbarItem(sender, plugin.getServer().getPlayer(args[2]), panel.getConfig(),true);
                    return true;
                }
            }
        }
        sender.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Usage: /cp <panel> [player:item] [player]"));
        return true;
    }
}
