package com.ar.askgaming.betterclans;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.betterclans.Clan.Clan;
import com.ar.askgaming.betterclans.Clan.ClanChat.ChatType;
import com.ar.askgaming.betterclans.Managers.ClansManager;
import com.ar.askgaming.betterclans.Managers.ClansManager.Permission;
import com.ar.askgaming.betterclans.Managers.FilesManager;

public class Commands implements TabExecutor{

    private BetterClans plugin;
    private ClansManager clans;
    private FilesManager files;
    public Commands(BetterClans plugin) {
        this.plugin = plugin;
        clans = plugin.getClansManager();
        files = plugin.getFilesManager();
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
        Player p = (Player) sender;

        if (args.length == 0){
            p.sendMessage(files.getLang("help", p));
            return true;
        }

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
            p.sendMessage(files.getLang("commands.invalid_usage", p));
            return;
        }
        if (clans.hasClan(p)){
            p.sendMessage(files.getLang("clan.already_in_clan", p));
            return;
        }
        String name = args[1];

        if (!plugin.getUtilityMethods().isAlphaNumeric(name)){
            p.sendMessage(files.getLang("commands.invalid_name", p));
            return;

        }
        if (plugin.getUtilityMethods().hasValidLength(name, 6, 16)){
            clans.createClan(name, p);
        } else p.sendMessage(files.getLang("commands.character_limit", p).replace("{min}", "6").replace("{max}", "16"));
        
    }
    //#region remove
    public void removeClan(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);
        if (clans.removeClan(clan)){
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage(files.getLang("misc.delete_broadcast", p).replace("{clan}", clan.getName()));
            });
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
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage(files.getLang("clan.no_clan", p));
            return;
        }
        if (args.length < 2){
            p.sendMessage(files.getLang("commands.invalid_usage", p));
            return;
        }

        if (!clans.hasClanPermission(p, Permission.valueOf(args[0].toUpperCase()))){
            return;
        }

        String s = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        if (!plugin.getUtilityMethods().isAlphaNumeric(s)){
            p.sendMessage(files.getLang("commands.invalid_name", p));
            return;
        }
        String set = "";
        switch (args[1].toLowerCase()) {
            case "home":
                clan.setHome(p.getLocation());
                set = "home";
                break;
            case "tag":

                if (plugin.getUtilityMethods().hasValidLength(s, 3, 8)){
                    clan.setTag(s);
                    set = "tag";
                } else p.sendMessage(files.getLang("commands.character_limit", p).replace("{min}", "3").replace("{max}", "8"));
   
                break;
            case "description":
                if (plugin.getUtilityMethods().hasValidLength(s, 8, 32)){
                    clan.setDescription(s);
                    set = "description";
                } else p.sendMessage(files.getLang("commands.character_limit", p).replace("{min}", "8").replace("{max}", "32"));
                break;  
            case "name":
                if (plugin.getUtilityMethods().hasValidLength(s, 6, 16)){
                    clan.setName(s);
                    set = "name";
                } else p.sendMessage(files.getLang("commands.character_limit", p).replace("{min}", "6").replace("{max}", "16"));
                break;      
            default:
                p.sendMessage(files.getLang("commands.invalid_usage", p));
                break;
        }
        p.sendMessage(files.getLang("clan.set", p).replace("{set}", set));
    }
    //#region home
    public void home(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);
        if (clans.hasClanPermission(p, Permission.HOME)){
            if (clan.getHome() == null){
                p.sendMessage(files.getLang("clan.no_home", p));
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
            p.sendMessage(files.getLang("commands.invalid_usage", p));
            return;
        }
        Player invited = plugin.getServer().getPlayer(args[1]);
        if (invited == null){
            p.sendMessage(files.getLang("clan.no_online", p));
            return;
        }
        int limit = plugin.getConfig().getInt("clan.limit");
        if (clan.getMembers().size() + clan.getRecruits().size() + clan.getOfficers().size() >= limit){
            p.sendMessage(files.getLang("clan.full", p).replace("{limit}", String.valueOf(limit)));
            return;

        }
        if (clans.getClanByPlayer(invited) != null){
            p.sendMessage(files.getLang("clan.player_already_in_clan", p));
            return;
        }
        if (clans.getInvited().containsValue(invited)){
            p.sendMessage(files.getLang("clan.already_invited", p));
            return;
        }

        clans.getInvited().put(clan.getName(), invited);
        p.sendMessage(files.getLang("clan.invite", p).replace("{player}", invited.getName()));
        invited.sendMessage(files.getLang("clan.invite_received", p).replace("{clan}", clan.getName()));
    }
    //#region join
    public void joinClan(Player p, String[] args){

        if (clans.hasClan(p)){
            p.sendMessage(files.getLang("clan.already_in_clan", p));
            return;
        }
        if (args.length < 2){
            p.sendMessage(files.getLang("commands.invalid_usage", p));
            return;
        }
        for (Map.Entry<String, Player> entry : clans.getInvited().entrySet()){
            if (entry.getValue().equals(p) && entry.getKey().equalsIgnoreCase(args[1])){
                
                Clan invited = clans.getClanByName(args[1]);
                if (invited == null){
                    p.sendMessage(files.getLang("clan.no_exists", p));
                    return;
                }
                p.sendMessage(files.getLang("clan.join", p).replace("{clan}", args[1]));
                invited.getRecruits().add(p.getUniqueId());
                invited.save();
                return;
            }
        }
        p.sendMessage(files.getLang("clan.no_invited", p));
    }
    //#region leave
    public void leaveClan(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage(files.getLang("clan.no_clan", p));
            return;
        }
        if (clan.getOwner().equals(p.getUniqueId())){
            p.sendMessage(files.getLang("misc.cant_leave", p));
            return;
        }
        clan.removePlayerFromClan(clan, p);
        p.sendMessage(files.getLang("clan.leave", p));
    }
    //#region kick
    public void kickPlayer(Player p, String[] args){

        if (!clans.hasClanPermission(p, Permission.KICK)){
            return;
        }

        if (args.length < 2){
            p.sendMessage(files.getLang("commands.invalid_usage", p));
            return;
        }
        Clan clan = clans.getClanByPlayer(p);
        OfflinePlayer kicked = Bukkit.getPlayer(args[1]);

        if (clan.getOwner().equals(kicked.getUniqueId())){
            p.sendMessage(files.getLang("clan.no_permission", p));
            return;
        }
        if (!clans.isInClan(clan, kicked)){
            p.sendMessage(files.getLang("clan.no_in_clan", p));
            return;
        }
        clan.removePlayerFromClan(clan, kicked.getPlayer());
        p.sendMessage(files.getLang("clan.kick", p).replace("{player}", args[1]));
        if (kicked.isOnline()){
            kicked.getPlayer().sendMessage(files.getLang("clan.kicked", p).replace("{clan}", clan.getName()));
        }
    }
    //#region promote
    public void promotePlayer(Player p, String[] args){
        if (!clans.hasClanPermission(p, Permission.PROMOTE)){
            return;
        }

        Clan clan = clans.getClanByOwner(p);
        OfflinePlayer promoted = plugin.getServer().getPlayer(args[1]);
        if (!clans.isInClan(clan, promoted)){
            p.sendMessage(files.getLang("clan.no_in_clan", p));
            return;

        }
        if (clan.promotePlayer(clan, promoted)){
            p.sendMessage(files.getLang("clan.promote", p).replace("{player}", args[1]));
            if (promoted.isOnline()){
                promoted.getPlayer().sendMessage(files.getLang("clan.promoted", p).replace("{clan}", clan.getName()));
            }
        } else p.sendMessage(files.getLang("misc.cant_promote_demote", p));
    }
    //#endregion
    //#region demote
    public void demotePlayer(Player p, String[] args){
        if (!clans.hasClanPermission(p, Permission.DEMOTE)){
            return;
        }
        Clan clan = clans.getClanByOwner(p);
        OfflinePlayer demoted = plugin.getServer().getPlayer(args[1]);
        if (!clans.isInClan(clan, demoted)){
            p.sendMessage(files.getLang("clan.no_in_clan", p));
            return;

        }

        if (clan.demotePlayer(clan, demoted)){
            p.sendMessage(files.getLang("clan.demote", p).replace("{player}", args[1]));
            if (demoted.isOnline()){
                demoted.getPlayer().sendMessage(files.getLang("clan.demoted", p).replace("{clan}", clan.getName()));
            }
        }else p.sendMessage(files.getLang("misc.cant_promote_demote", p));
    }
    //#endregion
    //#region info
    public void infoClan(Player p, String[] args){

        if (args.length < 2){
            Clan clan = clans.getClanByPlayer(p);
            if (clan == null){
                p.sendMessage(files.getLang("clan.no_clan", p));
                return;
            }
            clans.sendInfo(clan, p);
            return;
        }
        Clan clan = clans.getClanByName(args[1]);
        if (clan == null){
            p.sendMessage(files.getLang("clan.no_exists", p));
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
        if (args.length < 2) {
            p.sendMessage(files.getLang("commands.usage", p));
            return;
        }

        Clan ally = clans.getClanByName(args[1]);
        if (ally == null){
            p.sendMessage(files.getLang("clan.no_exists", p));
            return;
        }

        //If the clan is already an ally, remove it
        Clan clan = clans.getClanByPlayer(p);
        if (ally.equals(clan)){
            p.sendMessage(files.getLang("clan.cant_self", p));
            return;
        }
        if (clan.getAllies().contains(ally.getName())){
            clan.removeAlly(ally);
            ally.removeAlly(clan);
            clans.getAllClanMembers(clan).forEach(player -> {
                player.sendMessage(files.getLang("clan.ally_removed", p).replace("{clan}", ally.getName()));
            });
            clans.getAllClanMembers(ally).forEach(player -> {
                player.sendMessage(files.getLang("clan.ally_removed", p).replace("{clan}", clan.getName()));
            });

            return;
        }
        //Else send the request
        if (clans.getInvitedAlly().containsKey(clan) && clans.getInvitedAlly().get(clan).equals(ally)) {
            p.sendMessage(files.getLang("clan.already_ally_invited", p));
            return;
        }
        // Verifica si la solicitud de alianza ha sido aceptada
        if (clans.getInvitedAlly().containsKey(ally) && clans.getInvitedAlly().get(ally).equals(clan)) {
            ally.addAlly(clan);
            clan.addAlly(ally);
            clans.getInvitedAlly().remove(clan);
            clans.getInvitedAlly().remove(ally);
            clans.getAllClanMembers(clan).forEach(player -> {
                player.sendMessage(files.getLang("clan.ally", p).replace("{clan}", ally.getName()));
            });
            clans.getAllClanMembers(ally).forEach(player -> {
                player.sendMessage(files.getLang("clan.ally", p).replace("{clan}", clan.getName()));
            });
            return;
        }

        // Envía una solicitud de alianza
        clans.getInvitedAlly().put(clan, ally);
        p.sendMessage(files.getLang("clan.ally_sent", p).replace("{clan}", ally.getName()));
        clans.getAllClanMembers(ally).forEach(player -> {
            player.sendMessage(files.getLang("clan.ally_request", p).replace("{clan}", clan.getName()));
        });
    }
    //#endregion
    //#region enemy
    public void enemyClan(Player p, String[] args){
        if (!clans.hasClanPermission(p, Permission.ENEMY)){
            return;
        }
        if (args.length < 2){
            p.sendMessage(files.getLang("commands.invalid_usage", p));
            return;
        }
        Clan enemy = clans.getClanByName(args[1]);

        if (enemy == null){
            p.sendMessage(files.getLang("clan.no_exists", p));
            return;
        }
        Clan clan = clans.getClanByPlayer(p);
        if (clan.getAllies().contains(enemy.getName())){
            p.sendMessage(files.getLang("clan.cant_ally_enemy", p));
            return;

        }
        if (enemy.equals(clan)){
            p.sendMessage(files.getLang("clan.cant_self", p));
            return;
        }
        if (clan.getEnemies().contains(enemy.getName())){
            clan.removeEnemy(enemy);
            p.sendMessage(files.getLang("clan.enemy_removed", p).replace("{clan}", enemy.getName()));
            return;
        }
        clan.addEnemy(enemy);
        p.sendMessage(files.getLang("clan.enemy", p).replace("{clan}", enemy.getName()));
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
        p.sendMessage(files.getLang("help", p));
    }
    //#region list
    public void listClans(Player p, String[] args){
        List<String> allClans = clans.getAllClans();
        plugin.getUtilityMethods().listToPage(allClans, args, p);
    }
    //#region chat
    public void chatClan(Player p, String[] args){
        if (args.length < 2){
            p.sendMessage(files.getLang("commands.usage", p));
            return;
        }
        ChatType chatType;
        String message;
    
        switch (args[1].toLowerCase()) {
            case "a":
            case "ally":
                chatType = ChatType.ALLY;
                message = "Ally";
                break;
            case "c":
            case "clan":
                chatType = ChatType.CLAN;
                message = "Clan";
                break;
            case "g":
            case "global":
                chatType = ChatType.GLOBAL;
                message = "Global";
                break;
            default:
             p.sendMessage(files.getLang("commands.usage", p));
                return;
        }
    
        plugin.getClanChat().setChatType(p, chatType);
        p.sendMessage(files.getLang("clan.chat", p).replace("{type}", message));
    }
}
