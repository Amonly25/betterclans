package com.ar.askgaming.betterclans.Clan;

public class ClanWar {

    private Clan clan1;
    private Clan clan2;
    private int points1;
    private int points2;
    private boolean finished;
    private long timeleft;

    public ClanWar(Clan clan1, Clan clan2) {
        this.clan1 = clan1;
        this.clan2 = clan2;
        this.points1 = 0;
        this.points2 = 0;
        this.finished = false;
        this.timeleft = 24 * 60 * 60 * 1000;
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
    public long getTimeleft() {
        return timeleft;
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
