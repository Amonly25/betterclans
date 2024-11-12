package com.ar.askgaming.betterclans;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.betterclans.Clan.Clan;
import com.ar.askgaming.betterclans.Clan.ClanChat.ChatType;
import com.ar.askgaming.betterclans.Managers.ClansManager;
import com.ar.askgaming.betterclans.Managers.ClansManager.Permission;

public class Commands implements TabExecutor{

    private BetterClans plugin;
    private ClansManager clans;
    public Commands(BetterClans plugin) {
        this.plugin = plugin;
        clans = plugin.getClansManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of("create", "remove", "inventory", "set", "home", "invite", "join", "leave", "kick", "ally", "enemy", "war","shop","help");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage("You must be a player to use this command");
            return true;
        }
        if (args.length < 1){
            sender.sendMessage("Usage: /clan help");
            return true;

        }
        Player p = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "create":
                createClan(p, args);
                break;
            case "remove":
                removeClan(p, args);
                break;
            case "inventory":
                openInventoryClan(p, args);
                break;
            case "invite":
                invitePlayer(p, args);
                break;
            case "join":
                joinClan(p,args);
                break;
            case "leave":
                leaveClan(p, args);
                break;
            case "kick":
                kickPlayer(p, args);
                break;
            case "promote":
                promotePlayer(p, args);
                break;
            case "demote":
                demotePlayer(p, args);
                break;
            case "ally":
                allyClan(p, args);
                break;
            case "enemy":
                enemyClan(p, args);
                break;
            case "war":
                warClan(p, args);
                break;
            case "set":
                set(p, args);
                break;
            case "shop":
                shop(p, args);
                break;
            case "home":
                home(p, args);
                break;        
            case "info":
                infoClan(p, args);
                break;
            case "help":
                help(p, args);
                break;
            case "list":
                listClans(p, args);
                break;    
            case "chat":
                chatClan(p, args);
                break;
            default:
                help(p, args);
                break;
        }
        return true;
    }
    //#region create
    public void createClan(Player p, String[] args){
        if (args.length < 2){
            p.sendMessage("Usage: /clan create <name>");
            return;
        }
        if (clans.hasClan(p)){
            p.sendMessage("You are already in a clan");
            return;
        }
        String name = args[1];

        if (plugin.getUtilityMethods().hasValidLength(name, 6, 16)){
            clans.createClan(name, p);
        } else p.sendMessage("The name must be between 6 and 16 characters");
        
    }
    //#region remove
    public void removeClan(Player p, String[] args){
        if (clans.hasClanPermission(p, Permission.REMOVE)){

            if (clans.removeClan(clans.getClanByPlayer(p))){
                p.sendMessage("Clan removed successfully");
            } 
        } 
    }
    //#region inventory
    public void openInventoryClan(Player p, String[] args){

        if (clans.hasClanPermission(p, Permission.INVENTORY)){
            p.openInventory(clans.getClanByPlayer(p).getInventory());
            return;
        } 
    }
    //#region set
    public void set(Player p, String[] args){
        // CONFIG ALL THE SETTINGS
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage("You are not in a clan");
            return;
        }
        if (args.length < 2){
            p.sendMessage("Usage: /clan sethome");
            return;
        }

        if (!clans.hasClanPermission(p, Permission.valueOf(args[0].toUpperCase()))){
            return;
        }

        String s = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        switch (args[1].toLowerCase()) {
            case "home":
                clan.setHome(p.getLocation());
                p.sendMessage("Home set successfully");
                break;
            case "tag":
                if (plugin.getUtilityMethods().hasValidLength(s, 3, 8)){
                    clan.setTag(s);
                    p.sendMessage("Tag set successfully");
                } else p.sendMessage("The name must be between 3 and 8 characters");
   
                break;
            case "description":
                if (plugin.getUtilityMethods().hasValidLength(s, 8, 32)){
                    clan.setDescription(s);
                    p.sendMessage("Description set successfully");
                } else p.sendMessage("The name must be between 8 and 32 characters");
                break;  
            case "name":
                if (plugin.getUtilityMethods().hasValidLength(s, 6, 16)){
                    clan.setName(s);
                    p.sendMessage("Name set successfully");
                } else p.sendMessage("The name must be between 6 and 16 characters");
                break;      
            default:
                p.sendMessage("Usage: /clan set <home/tag/description/name>");
                break;
        }
    }
    //#region home
    public void home(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);
        if (clans.hasClanPermission(p, Permission.HOME)){
            if (clan.getHome() == null){
                p.sendMessage("The clan doesn't have a home");
                return;
            }
            plugin.getUtilityMethods().teleport(p, clan.getHome());
        } 
    }
    //#region invite
    public void invitePlayer(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);
        if (!clans.hasClanPermission(p, Permission.INVITE)){
            return;
        }

        if (args.length < 2){
            p.sendMessage("Usage: /clan invite <player>");
            return;
        }
        Player invited = plugin.getServer().getPlayer(args[1]);
        if (invited == null){
            p.sendMessage("The player is not online");
            return;
        }
        if (clans.getClanByPlayer(invited) != null){
            p.sendMessage("The player is already in a clan");
            return;
        }
        if (clans.getInvited().containsValue(invited)){
            p.sendMessage("The player is already invited");
            return;
        }

        clans.getInvited().put(clan.getName(), invited);
        p.sendMessage("Player invited successfully");
        invited.sendMessage("You have been invited to join the clan " + clan.getName());
    }
    //#region join
    public void joinClan(Player p, String[] args){

        if (clans.hasClan(p)){
            p.sendMessage("You are already in a clan");
            return;
        }
        if (args.length < 2){
            p.sendMessage("Usage: /clan join <clan>");
            return;
        }
        for (Map.Entry<String, Player> entry : clans.getInvited().entrySet()){
            if (entry.getValue().equals(p) && entry.getKey().equalsIgnoreCase(args[1])){
                p.sendMessage("You have joined the clan " + entry.getKey());
                Clan invited = clans.getClanByName(entry.getKey());
                invited.getRecruits().add(p.getUniqueId());
                invited.save();
                return;
            }
        }
        p.sendMessage("You haven't been invited to join the clan");
    }
    //#region leave
    public void leaveClan(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage("You are not in a clan");
            return;
        }
        if (clan.getOwner().equals(p.getUniqueId())){
            p.sendMessage("You can't leave the clan because you are the owner");
            return;
        }
        clan.removePlayerFromClan(clan, p);
        p.sendMessage("You have left the clan");
    }
    //#region kick
    public void kickPlayer(Player p, String[] args){

        if (!clans.hasClanPermission(p, Permission.KICK)){
            return;
        }

        if (args.length < 2){
            p.sendMessage("Usage: /clan kick <player>");
            return;
        }
        Clan clan = clans.getClanByPlayer(p);
        OfflinePlayer kicked = plugin.getServer().getPlayer(args[1]);

        if (clan.getOwner().equals(kicked.getUniqueId())){
            p.sendMessage("You can't kick the owner of the clan");
            return;
        }
        if (clans.isInClan(clan, kicked)){
            p.sendMessage("The player is not in the clan");
            return;
        }
        clan.removePlayerFromClan(clan, kicked.getPlayer());
        p.sendMessage("Player kicked successfully");
        if (kicked.isOnline()){
            kicked.getPlayer().sendMessage("You have been kicked from the clan " + clan.getName());
        }
    }
    //#region promote
    public void promotePlayer(Player p, String[] args){
        if (!clans.hasClanPermission(p, Permission.PROMOTE)){
            return;
        }

        Clan clan = clans.getClanByOwner(p);
        OfflinePlayer promoted = plugin.getServer().getPlayer(args[1]);
        clan.promotePlayer(clan, promoted);
    }
    //#endregion
    //#region demote
    public void demotePlayer(Player p, String[] args){
        if (!clans.hasClanPermission(p, Permission.DEMOTE)){
            return;
        }
        Clan clan = clans.getClanByOwner(p);
        OfflinePlayer demoted = plugin.getServer().getPlayer(args[1]);
        clan.demotePlayer(clan, demoted);
    }
    //#endregion
    //#region info
    public void infoClan(Player p, String[] args){

        if (args.length < 2){
            Clan clan = clans.getClanByPlayer(p);
            if (clan == null){
                p.sendMessage("You are not in a clan");
                return;
            }
            clans.sendInfo(clan, p);
            return;
        }
        Clan clan = clans.getClanByName(args[1]);
        if (clan == null){
            p.sendMessage("The clan doesn't exist");
            return;
        }
        clans.sendInfo(clan, p);
    }
    //#endregion
    //#region ally
    public void allyClan(Player p, String[] args){
        if (!clans.hasClanPermission(p, Permission.ALLY)){
            return;
        }

        Clan ally = clans.getClanByName(args[1]);
        if (ally == null){
            p.sendMessage("The clan doesn't exist");
            return;
        }
        Clan clan = clans.getClanByPlayer(p);
        if (clan.getAllies().contains(ally.getName())){
            clan.removeAlly(ally);
            p.sendMessage("Ally removed successfully");
            return;
        }
        clan.addAlly(ally);
        p.sendMessage("Ally added successfully");
    }
    //#endregion
    //#region enemy
    public void enemyClan(Player p, String[] args){
        if (!clans.hasClanPermission(p, Permission.ENEMY)){
            return;
        }
        if (args.length < 2){
            p.sendMessage("Usage: /clan enemy <clan>");
            return;
        }
        Clan enemy = clans.getClanByName(args[1]);
        if (enemy == null){
            p.sendMessage("The clan doesn't exist");
            return;
        }
        Clan clan = clans.getClanByPlayer(p);
        if (clan.getEnemies().contains(enemy.getName())){
            clan.removeEnemy(enemy);
            p.sendMessage("Enemy removed successfully");
            return;
        }
        clan.addEnemy(enemy);
    
        p.sendMessage("Enemy added successfully");
    }
    //#endregion
    //#region war
    public void warClan(Player p, String[] args){
        p.sendMessage("En desarrollo");

    }
    //#endregion
    //#region shop
    public void shop(Player p, String[] args){
        p.sendMessage("En desarrollo");
    }
    //#endregion
    //#region help
    public void help(Player p, String[] args){
        p.sendMessage("Usage: /clan <create/remove/inventory/set/home/invite/join/leave/kick/promote/demote/ally/enemy/war/shop/help>");
    }
    //#region list
    public void listClans(Player p, String[] args){
        List<String> allClans = clans.getAllClans();
        plugin.getUtilityMethods().listToPage(allClans, args, p);
    }
    //#region chat
    public void chatClan(Player p, String[] args){
        if (args.length < 2){
            p.sendMessage("Usage: /clan chat <clan/global/ally>");
            return;
        }
        ChatType chatType;
        String message;
    
        switch (args[1].toLowerCase()) {
            case "a":
            case "ally":
                chatType = ChatType.ALLY;
                message = "Chat type set to Ally";
                break;
            case "c":
            case "clan":
                chatType = ChatType.CLAN;
                message = "Chat type set to Clan";
                break;
            case "g":
            case "global":
                chatType = ChatType.GLOBAL;
                message = "Chat type set to Global";
                break;
            default:
                p.sendMessage("Usage: /clan chat <clan/global/ally>");
                return;
        }
    
        plugin.getClanChat().setChatType(p, chatType);
        p.sendMessage(message);
    }
}
