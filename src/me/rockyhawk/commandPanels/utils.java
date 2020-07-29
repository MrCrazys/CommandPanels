package me.rockyhawk.commandPanels;

import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class utils implements Listener {
    commandpanels plugin;
    public utils(commandpanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onPanelClick(InventoryClickEvent e) {
        //when clicked on a panel
        String tag = plugin.config.getString("config.format.tag") + " ";
        Player p = (Player)e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        try {
            if(e.getView().getType() != InventoryType.CHEST){
                //if it isn't a chest interface
                return;
            }
            if(ChatColor.stripColor(e.getView().getTitle()).equals("Chest") || ChatColor.stripColor(e.getView().getTitle()).equals("Large Chest") || ChatColor.stripColor(e.getView().getTitle()).equals("Trapped Chest")){
                //if the inventory is just a chest that has no panel
                return;
            }
            if (plugin.panelFiles == null) {
                //if no panels are present
                return;
            }
            if(clicked == null){
                //if itemstack is null
                return;
            }
        }catch(Exception b){
            return;
        }
        YamlConfiguration cf = null; //this is the file to use for any panel.* requests
        String panel = null;
        boolean foundPanel = false;
        for (String filename : plugin.panelFiles) { //will loop through all the files in folder
            String key;
            YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + filename));
            if (!plugin.checkPanels(temp)) {
                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + ": Syntax error Found or Missing certain element!"));
                return;
            }
            for (String s : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)) {
                key = s;
                if (ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(temp.getString("panels." + key + ".title"))).equals(e.getView().getTitle())) {
                    panel = key;
                    cf = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + filename));
                    foundPanel = true;
                    break;
                }
            }
            if (foundPanel) {
                //this is to avoid the plugin to continue looking when it was already found
                break;
            }
        }
        if(panel == null){
            return;
        }
        if(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(cf.getString("panels." + panel + ".title")))).equals("Command Panels Editor")){
            //cancel if it is the editor (this should never happen unless the user made a panel called Command Panels Editor for some reason)
            return;
        }
        if(e.getSlotType().equals(InventoryType.SlotType.CONTAINER) && e.getRawSlot() <= Integer.parseInt(Objects.requireNonNull(cf.getString("panels." + panel + ".rows")))*9-1){
            e.setCancelled(true);
            p.updateInventory();
            //this loops through all the items in the panel
            boolean foundSlot = false;
            for(String slot : Objects.requireNonNull(cf.getConfigurationSection("panels." + panel + ".item")).getKeys(false)){
                if(slot.equals(Integer.toString(e.getSlot()))){
                    foundSlot = true;
                }
            }
            if(!foundSlot){
                return;
            }
            //loop through possible hasvalue/hasperm 1,2,3,etc
            String section = plugin.hasSection(panel, cf, e.getSlot(),p);
            //this will remove any pending user inputs, if there is already something there from a previous item
            for(int o = 0; plugin.userInputStrings.size() > o; o++){
                if(plugin.userInputStrings.get(o)[0].equals(p.getName())){
                    plugin.userInputStrings.remove(o);
                    o=o-1;
                }
            }
            if(cf.contains("panels." + panel + ".item." + e.getSlot() + section + ".commands")) {
                List<String> commands = cf.getStringList("panels." + panel + ".item." + e.getSlot() + section + ".commands");
                assert commands != null;
                if (commands.size() != 0) {
                    //this will replace a sequence tag command with the commands from the sequence
                    List<String> commandsAfterSequence = commands;
                    for (int i = 0; commands.size() - 1 >= i; i++) {
                        if(commands.get(i).startsWith("sequence=")){
                            String locationOfSequence = commands.get(i).split("\\s")[1];
                            List<String> commandsSequence = cf.getStringList(locationOfSequence);
                            commandsAfterSequence.remove(i);
                            assert commandsSequence != null;
                            commandsAfterSequence.addAll(i,commandsSequence);
                        }
                    }
                    commands = commandsAfterSequence;
                    for (int i = 0; commands.size() - 1 >= i; i++) {
                        try {
                            if (commands.get(i).split("\\s")[0].equalsIgnoreCase("right=")) {
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("right=", "").trim());
                                commands.set(i, commands.get(i).replace("RIGHT=", "").trim());
                                if (e.isLeftClick() || (e.isShiftClick() && e.isLeftClick()) || (e.isShiftClick() && e.isRightClick())) {
                                    continue;
                                }
                            } else if (commands.get(i).split("\\s")[0].equalsIgnoreCase("rightshift=")) {
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("rightshift=", "").trim());
                                commands.set(i, commands.get(i).replace("RIGHTSHIFT=", "").trim());
                                if (e.isLeftClick() || (!e.isShiftClick() && e.isRightClick())) {
                                    continue;
                                }
                            }
                            if (commands.get(i).split("\\s")[0].equalsIgnoreCase("left=")) {
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("left=", "").trim());
                                commands.set(i, commands.get(i).replace("LEFT=", "").trim());
                                if (e.isRightClick() || (e.isShiftClick() && e.isRightClick()) || (e.isShiftClick() && e.isLeftClick())) {
                                    continue;
                                }
                            } else if (commands.get(i).split("\\s")[0].equalsIgnoreCase("leftshift=")) {
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("leftshift=", "").trim());
                                commands.set(i, commands.get(i).replace("LEFTSHIFT=", "").trim());
                                if (e.isRightClick() || (!e.isShiftClick() && e.isLeftClick())) {
                                    continue;
                                }
                            }
                            if (!e.isLeftClick() && !e.isRightClick()) {
                                continue;
                            }
                            if (clicked == null) {
                                continue;
                            }
                        } catch (Exception click) {
                            //skip if you can't do this
                        }
                        try {
                            assert clicked != null;
                            commands.set(i, commands.get(i).replaceAll("%cp-clicked%", clicked.getType().toString()));
                        } catch (Exception mate) {
                            commands.set(i, commands.get(i).replaceAll("%cp-clicked%", "Air"));
                        }
                        //end custom PlaceHolders
                        int val = plugin.commandPayWall(p,commands.get(i));
                        if(val == 0){
                            return;
                        }
                        if(val == 2){
                            plugin.commandTags(p, commands.get(i));
                        }
                    }
                }
            }
        }
        //stop duplicate
        p.updateInventory();
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if(p.isOp() || p.hasPermission("*.*")){
            if(plugin.update) {
                p.sendMessage(ChatColor.WHITE + "CommandPanels " + ChatColor.DARK_RED + "is not running the latest version! A new version is available at");
                p.sendMessage(ChatColor.RED + "https://www.spigotmc.org/resources/command-panels-custom-guis.67788/");
            }
        }
    }
}
