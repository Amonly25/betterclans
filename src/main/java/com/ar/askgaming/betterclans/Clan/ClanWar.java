package com.ar.askgaming.betterclans.Clan;

import com.ar.askgaming.betterclans.BetterClans;

public class ClanWar {

    private Clan clan1, clan2;
    private int points1, points2;
    private boolean finished;
    private int timeleft;

    private final BetterClans plugin = BetterClans.getInstance();

    public ClanWar(Clan clan1, Clan clan2) {
        this.clan1 = clan1;
        this.clan2 = clan2;
        this.points1 = 0;
        this.points2 = 0;
        this.finished = false;
        this.timeleft = plugin.getConfig().getInt("war.duration_minutes",1440);
    }

    public Clan getClan1() {
        return clan1;
    }

    public Clan getClan2() {
        return clan2;
    }

    public int getPoints1() {
        return points1;
    }

    public int getPoints2() {
        return points2;
    }
    public Integer getTimeleft() {
        return timeleft;
    }
    public void setTimeleft(Integer timeleft) {
        this.timeleft = timeleft;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setPoints1(int points1) {
        this.points1 = points1;
    }

    public void setPoints2(int points2) {
        this.points2 = points2;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void addPoints1(int points) {
        this.points1 += points;
    }

    public void addPoints2(int points) {
        this.points2 += points;
    }

    public void finish() {
        this.finished = true;
    }
}
