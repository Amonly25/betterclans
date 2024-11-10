package com.ar.askgaming.betterclans;

import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class Commands implements TabExecutor{

    private BetterClans plugin;
    private ClansManager clans;
    public Commands(BetterClans plugin) {
        this.plugin = plugin;
        clans = plugin.getClansManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of("create", "remove", "inventory", "sethome", "home", "invite", "join", "leave", "kick", "ally", "enemy", "war","shop","help");
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
            case "sethome":
                setHome(p, args);
                break;
            case "shop":
                shop(p, args);
                break;
            case "home":
                home(p, args);
                break;        
            default:
                infoClan(p, args);
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
        if (name.length() > 16){
            p.sendMessage("The name of the clan can't be longer than 16 characters");
            return;
        }
        if (name.length() < 4){
            p.sendMessage("The name of the clan can't be shorter than 4 characters");
            return;
        }
        if (clans.getClanByName(name.toLowerCase()) != null){
            p.sendMessage("The clan name is already in use");
            return;
        }
        //Check vault and then create
        if (clans.createClan(name, p)){
            p.sendMessage("Clan created successfully");
        } else {
            p.sendMessage("An error occurred while creating the clan");
        }
    }
    //#region remove
    public void removeClan(Player p, String[] args){
        Clan clan = clans.getClanByOwner(p);
        if (clan == null){
            return;
        }

        if (clans.removeClan(clan)){
            p.sendMessage("Clan removed successfully");
        }
    }
    //#region inventory
    public void openInventoryClan(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage("You are not in a clan");
            return;
        }
        if (clans.hasInventoryPermission(p)){
            p.openInventory(clan.getInventory());
            return;

        }
        p.sendMessage("You don't have permission to open the clan inventory");
    }
    //#region sethome
    public void setHome(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage("You are not in a clan");
            return;
        }
        if (clans.hasSetHomePermission(p)){
            clan.setHome(p.getLocation());
            p.sendMessage("Home set successfully");
            return;
        }
        p.sendMessage("You don't have permission to set the home");
    }
    //#region home
    public void home(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage("You are not in a clan");
            return;
        }
        if (clan.getHome() == null){
            p.sendMessage("The clan doesn't have a home");
            return;
        }
        if (!clans.hasHomePermission(p)){
            p.sendMessage("No tienes permiso para ir a la base del clan");
            return;
        }
        clans.teleport(p, clan.getHome());
    }
    //#region invite
    public void invitePlayer(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage("You are not in a clan");
            return;
        }
        if (!clans.hasKickInvitePermission(p)){
            p.sendMessage("You don't have permission to invite players");
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
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage("You are not in a clan");
            return;
        }
        if (clans.hasKickInvitePermission(p)){
            p.sendMessage("You don't have permission to kick players");
            return;
        }
        if (args.length < 2){
            p.sendMessage("Usage: /clan kick <player>");
            return;
        }
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
        Clan clan = clans.getClanByOwner(p);
        if (clan == null){
            p.sendMessage("No puedes hacer eso.");
            return;
        }
        if (args.length < 2){
            p.sendMessage("Usage: /clan promote <player>");
            return;
        }
        OfflinePlayer promoted = plugin.getServer().getPlayer(args[1]);
        clan.promotePlayer(clan, promoted);
    }
    //#endregion
    //#region demote
    public void demotePlayer(Player p, String[] args){
        Clan clan = clans.getClanByOwner(p);
        if (clan == null){
            p.sendMessage("No puedes hacer eso");
            return;
        }
        if (args.length < 2){
            p.sendMessage("Usage: /clan demote <player>");
            return;
        }
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
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage("You are not in a clan");
            return;
        }
        if (args.length < 2){
            p.sendMessage("Usage: /clan ally <clan>");
            return;
        }
        Clan ally = clans.getClanByName(args[1]);
        if (ally == null){
            p.sendMessage("The clan doesn't exist");
            return;
        }
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
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage("You are not in a clan");
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
        // Clan clan = clans.getClanByPlayer(p);
        // if (clan == null){
        //     p.sendMessage("You are not in a clan");
        //     return;
        // }
        // if (args.length < 2){
        //     p.sendMessage("Usage: /clan war <clan>");
        //     return;
        // }
        // Clan enemy = clans.getClanByName(args[1]);
        // if (enemy == null){
        //     p.sendMessage("The clan doesn't exist");
        //     return;
        // }
        // if (!clan.getEnemies().contains(enemy.getName())){
        //     p.sendMessage("The clan is not an enemy");
        //     return;
        // }
        // if (clan.getWars().contains(enemy.getName())){
        //     p.sendMessage("The clan is already at war");
        //     return;
        // }
        // clan.addWar(enemy);
        // p.sendMessage("War started successfully");
    }
    //#endregion
    //#region shop
    public void shop(Player p, String[] args){
        p.sendMessage("En desarrollo");
    }
    //#endregion

}
