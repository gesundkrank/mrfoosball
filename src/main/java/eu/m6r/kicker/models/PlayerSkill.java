package eu.m6r.kicker.models;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PlayerSkill implements Serializable {

    public PlayerSkill() {
    }

    @Id
    public String id;
    public String name;
    public String avatarImage;
    public int games;
    public int wins;
    public double skill;
}
