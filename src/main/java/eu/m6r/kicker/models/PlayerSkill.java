package eu.m6r.kicker.models;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

public class PlayerSkill implements Serializable {

    public PlayerSkill() {
    }

    public PlayerSkill(Object[] objects) {
        this.id = (String)objects[0];
        this.name = (String)objects[1];
        this.avatarImage = (String) objects[2];
        this.games = (Integer) objects[3];
        this.wins = (Integer) objects[4];
        this.skill = (Double) objects[5];
    }

    public String id;
    public String name;
    public String avatarImage;
    public int games;
    public int wins;
    public double skill;

    @Override
    public String toString() {
        return "PlayerSkill{" +
               ", games=" + games +
               ", wins=" + wins +
               ", skill=" + skill +
               '}';
    }
}
