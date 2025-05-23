package com.ar.askgaming.betterclans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.betterclans.Clan.Clan;
import com.ar.askgaming.betterclans.Clan.ClanChat.ChatType;
import com.ar.askgaming.betterclans.Clan.ClanShop;
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
        if (args.length == 1){
            return List.of("create", "delete", "inventory", "set", "home", "invite","deposit","withdraw", 
            "join", "leave", "kick", "ally", "enemy", "war","shop","help","info","list","chat","top");
        }
        if (args.length == 2){
            if (args[0].equalsIgnoreCase("set")){
                return List.of("home", "tag", "description", "name");
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            switch (args[0].toLowerCase()) {
                case "teleport":
                    if (args.length == 2){
                        Clan clan = clans.getClanByName(args[1]);
                        if (clan != null){
                            if (clan.getHome() == null){
                                sender.sendMessage("Clan has no home");
                                return true;
                            }
                            for (Player player : plugin.getClansManager().getAllClanMembers(clan)){
                                plugin.getUtilityMethods().teleport(player, clan.getHome(), true);
                            }
                        }
                        return true;
                    }
                    break;
                case "reload":
                    plugin.reloadConfig();
                    sender.sendMessage("Config reloaded");
                    plugin.setClanShop(new ClanShop(plugin));
                    break;
                default:
                    sender.sendMessage("That command is only for players or is not valid");
                    break;
            }
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
            case "delete":
                deleteClan(p, args);
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
            case "deposit":
                deposit(p, args);
                break;
            case "withdraw":
                withdraw(p, args);
                break;
            case "reload":
                if (p.hasPermission("betterclans.admin")){
                    plugin.reloadConfig();
                    p.sendMessage("Config reloaded");
                    plugin.setClanShop(new ClanShop(plugin));
                }
                break;
            case "top":
                top(p, args);
                break;
            case "rankup":
                rankup(p, args);
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
        Clan clan = clans.getClanByName(name);
        if (clan != null){
            p.sendMessage(files.getLang("clan.already_exists", p));
            return;
        }

        if (plugin.getUtilityMethods().hasValidLength(name, 6, 16)){

            clans.createClan(name, p);
            double cost = plugin.getConfig().getDouble("cost_creation", 50);

            if (plugin.getVaultEconomy() != null){
                if (plugin.getVaultEconomy().getBalance(p) < cost){
                    p.sendMessage(files.getLang("economy.not_enough", p).replace("{amount}", String.valueOf(cost)));
                    return;
                }
                plugin.getVaultEconomy().withdrawPlayer(p, cost);
                p.sendMessage(files.getLang("economy.clan_cost", p).replace("{cost}", String.valueOf(cost)));
            }

        } else p.sendMessage(files.getLang("commands.character_limit", p).replace("{min}", "6").replace("{max}", "16"));
        
    }
    //#region delete
    public void deleteClan(Player p, String[] args){
        
        Clan argClan = clans.getClanByName(args[1]);
        if (p.isOp() && argClan!= null){
            if (clans.deleteClan(argClan)){
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendMessage(files.getLang("misc.delete_broadcast", p).replace("{clan}", argClan.getName()));
                });
            } 
        }

        Clan clan = clans.getClanByPlayer(p);

        if (!clans.hasClanPermission(p, Permission.DELETE)){
            p.sendMessage(files.getLang("clan.no_permission", p));
            return;
        }
        boolean isEmpty = true;
        for (ItemStack item : clan.getInventory().getContents()){
            if (item != null){
                isEmpty = false;
                break;
            }
        }
        if (!isEmpty){
            p.sendMessage(files.getLang("clan.no_empty", p));
            return;
        }
        if (clans.deleteClan(clan)){
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage(files.getLang("misc.delete_broadcast", p).replace("{clan}", clan.getName()));
            });
        } 
    }
    //#region inventory
    public void openInventoryClan(Player p, String[] args){

        if (args.length == 2){
            if (p.hasPermission("betterclans.admin")){
                Clan clan = clans.getClanByName(args[1]);
                if (clan != null){
                    p.openInventory(clan.getInventory());
                    return;
                }
            } else {
                p.sendMessage(files.getLang("clan.no_permission", p));
            }
        } else if (clans.hasClanPermission(p, Permission.INVENTORY)){
            p.openInventory(clans.getClanByPlayer(p).getInventory());
            return;
        }
    }
    //#region set
    public void set(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);

        if (args.length < 2){
            p.sendMessage(files.getLang("commands.invalid_usage", p));
            return;
        }
        
        if (!clans.hasClanPermission(p, Permission.SET)){
            p.sendMessage(files.getLang("clan.no_permission", p));
            return;
        }

        String key = args[1].toLowerCase();
        String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
    
        if (!plugin.getUtilityMethods().isAlphaNumeric(value)) {
            p.sendMessage(files.getLang("commands.invalid_name", p));
            return;
        }
    
        // Mapeo de acciones
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("home", () -> {
            clan.setHome(p.getLocation());
            p.sendMessage(files.getLang("clan.set", p).replace("{set}", "home"));
        });
        actions.put("tag", () -> setClanAttribute(p, clan, value, 3, 8, "tag", () -> clan.setTag(value), clans.getClanByTag(value), "clan.tag_exists"));
        actions.put("description", () -> setClanAttribute(p, clan, value, 8, 32, "description", () -> clan.setDescription(value), null, null));
        actions.put("name", () -> setClanAttribute(p, clan, value, 6, 16, "name", () -> clan.setName(value), clans.getClanByName(value), "clan.already_exists"));

        // Ejecuta la acción correspondiente o envía error por defecto
        actions.getOrDefault(key, () -> p.sendMessage(files.getLang("commands.invalid_usage", p))).run();
    }

    private void setClanAttribute(Player p, Clan clan, String value, int min, int max, String attribute, Runnable setter, Clan existingClan, String existsLangKey) {
        if (existingClan != null) {
            p.sendMessage(files.getLang(existsLangKey, p));
            return;
        }

        if (!plugin.getUtilityMethods().hasValidLength(value, min, max)) {
            p.sendMessage(files.getLang("commands.character_limit", p)
                    .replace("{min}", String.valueOf(min))
                    .replace("{max}", String.valueOf(max)));
            return;
        }

        setter.run();
        p.sendMessage(files.getLang("clan.set", p).replace("{set}", attribute));
    }

    //#region home
    public void home(Player p, String[] args){
        Clan clan = clans.getClanByPlayer(p);
        if (clans.hasClanPermission(p, Permission.HOME)){
            if (clan.getHome() == null){
                p.sendMessage(files.getLang("clan.no_home", p));
                return;
            }
            plugin.getUtilityMethods().teleport(p, clan.getHome(),false);
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
        int level = clan.getLevel();
        int limit = plugin.getConfig().getInt("rankup."+String.valueOf(level)+".limit",5);
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
        Clan invited = clans.getClanByName(args[1]);

        if (p.isOp() && invited!= null){
            invited.getOfficers().add(p.getUniqueId());
            return;
        }
        for (Map.Entry<String, Player> entry : clans.getInvited().entrySet()){
            if (entry.getValue().equals(p) && entry.getKey().equalsIgnoreCase(args[1])){
                
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
    @SuppressWarnings("deprecation")
    public void kickPlayer(Player p, String[] args){

        if (!clans.hasClanPermission(p, Permission.KICK)){
            return;
        }

        if (args.length < 2){
            p.sendMessage(files.getLang("commands.invalid_usage", p));
            return;
        }
        Clan clan = clans.getClanByPlayer(p);
        OfflinePlayer kicked = Bukkit.getOfflinePlayer(args[1]);

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
    @SuppressWarnings("deprecation")
    public void promotePlayer(Player p, String[] args){
        if (!clans.hasClanPermission(p, Permission.PROMOTE)){
            return;
        }

        Clan clan = clans.getClanByOwner(p);
        OfflinePlayer promoted = Bukkit.getOfflinePlayer(args[1]);
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
    @SuppressWarnings("deprecation")
    public void demotePlayer(Player p, String[] args){
        if (!clans.hasClanPermission(p, Permission.DEMOTE)){
            return;
        }
        Clan clan = clans.getClanByOwner(p);
        OfflinePlayer demoted = Bukkit.getOfflinePlayer(args[1]);
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
    @SuppressWarnings("deprecation")
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
        if (clan != null){
            clans.sendInfo(clan, p);
            return;
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            Clan playerClan = clans.getClanByOfflinePlayer(player);
            if (playerClan != null){
                clans.sendInfo(playerClan, p);
                return;
            }
        }
        
        p.sendMessage(files.getLang("clan.no_exists", p));
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

        if (enemy.getEnemies().contains(clan.getName())){
            clans.startWar(clan, enemy);
        }
        clan.addEnemy(enemy);
        p.sendMessage(files.getLang("clan.enemy", p).replace("{clan}", enemy.getName()));
    }
    //#endregion
    //#region war
    public void warClan(Player p, String[] args){
       
        if (!clans.hasClanPermission(p, Permission.WAR)){
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
        if (!clan.getEnemies().contains(enemy.getName())){
            p.sendMessage(files.getLang("clan.not_enemy", p));
            return;
        }
        if (clans.isInWarWith(clan, enemy) || clans.isInWarWith(enemy, clan)){
            p.sendMessage(files.getLang("war.already_in", p));
            return;
        }
        if (enemy.getEnemies().contains(clan.getName())){
            clans.startWar(clan, enemy);
            return;
        }
    }
    //#endregion
    //#region shop
    public void shop(Player p, String[] args){
        p.openInventory(plugin.getClanShop().getInv());
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
    //#region deposit
    public void deposit(Player p, String[] args){
        if (args.length < 2){
            p.sendMessage(files.getLang("commands.usage", p));
            return;
        }
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage(files.getLang("clan.no_clan", p));
            return;
        }
        if (!clans.hasClanPermission(p, Permission.DEPOSIT)){
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            p.sendMessage(files.getLang("commands.invalid_number", p));
            return;
        }
        if (plugin.getVaultEconomy() != null){
            if (plugin.getVaultEconomy().getBalance(p) < amount){
                p.sendMessage(files.getLang("economy.not_enough", p).replace("{amount}", String.valueOf(amount)));
                return;
            }
            plugin.getVaultEconomy().withdrawPlayer(p, amount);
        }

        clan.setBalance(clan.getBalance() + amount);
        clan.save();
        p.sendMessage(files.getLang("economy.deposit", p).replace("{amount}", String.valueOf(amount)));
    }
    //#region withdraw
    public void withdraw(Player p, String[] args){
        if (args.length < 2){
            p.sendMessage(files.getLang("commands.usage", p));
            return;
        }
        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage(files.getLang("clan.no_clan", p));
            return;
        }
        if (!clans.hasClanPermission(p, Permission.WITHDRAW)){
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            p.sendMessage(files.getLang("commands.invalid_number", p));
            return;
        }
        if (clan.getBalance() < amount){
            p.sendMessage(files.getLang("economy.not_enough_clan", p).replace("{amount}", amount + ""));
            return;
        }
        if (plugin.getVaultEconomy() != null){
            plugin.getVaultEconomy().depositPlayer(p, amount);
        }
        clan.setBalance(clan.getBalance() - amount);
        clan.save();
        p.sendMessage(files.getLang("economy.withdraw", p).replace("{amount}", String.valueOf(amount)));
    }
    //#region top
    public void top(Player p, String[] args){

        HashMap<String, Integer> sorted = new HashMap<>();
        plugin.getClansManager().getClans().forEach((name, clan) -> {
            sorted.put(name, clan.getPoints());
        });

        List<String> allClans = new ArrayList<>();

        sorted.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).forEach(entry -> {
            allClans.add(entry.getKey() + " - " + entry.getValue());
        });

        plugin.getUtilityMethods().listToPage(allClans, args, p);
    }
    //#region rankup
    public void rankup(Player p, String[] args){


        if (args.length > 1){
            p.sendMessage(plugin.getFilesManager().getLang("rankup", p));
            return;
        }

        Clan clan = clans.getClanByPlayer(p);
        if (clan == null){
            p.sendMessage(files.getLang("clan.no_clan", p));
            return;
        }
        if (!clans.hasClanPermission(p, Permission.RANKUP)){
            return;
        }
        int level = clan.getLevel();
        if (level >= 5){
            p.sendMessage(files.getLang("clan.max_level", p));
            return;
        }
        int cost = plugin.getConfig().getInt("rankup." + String.valueOf(level+1)+".cost", 500);
        if (clan.getBalance() < cost){
            p.sendMessage(files.getLang("economy.not_enough_clan", p).replace("{amount}", cost + ""));
            return;
        }
        clan.setBalance(clan.getBalance() - cost);
        clan.setLevel(clan.getLevel() + 1);
        List<ItemStack> items = new ArrayList<>();
        ItemStack[] contents = clan.getInventory().getContents().clone();
        for (ItemStack item : contents){
            if (item != null){
                items.add(item);
            }
        }
        clan.getInventory().clear();
        clan.loadInventory(items);
        clan.save();
        p.sendMessage(files.getLang("clan.rankup", p));
    }

}
