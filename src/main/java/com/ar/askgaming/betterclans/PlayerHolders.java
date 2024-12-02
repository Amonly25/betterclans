package com.ar.askgaming.betterclans;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.ar.askgaming.betterclans.Clan.Clan;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlayerHolders extends PlaceholderExpansion{

    private BetterClans plugin;
    public PlayerHolders(BetterClans plugin) {
        this.plugin = plugin;
    }

    @Override
	public String onPlaceholderRequest(Player player, String identifier) {
		
        if(player == null){
            return "";
        }
        Clan clan = plugin.getClansManager().getClanByPlayer(player);
        if(clan == null) {
            return "";
        }

        switch (identifier.toLowerCase()) {
            case "tag":
                return clan.getTag();
            case "tag_formatted":
                return "§7[§8"+clan.getTag()+"§7]";
            case "clan":
                return clan.getName();
            case "points":
                return clan.getPoints()+"";
            case "balance":
                return clan.getBalance()+"";
            default:
                return "unknown";

        }
	}

    @Override
    public @NotNull String getAuthor() {
        return "AMONLY";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "betterclans";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }
    
}
