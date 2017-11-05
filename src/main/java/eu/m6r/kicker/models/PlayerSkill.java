package eu.m6r.kicker.models;

public class PlayerSkill {

    public PlayerSkill() {
    }

    public PlayerSkill(Object[] objects) {
        this.id = (String)objects[0];
        this.name = (String)objects[1];
        this.avatarImage = (String) objects[2];
        this.games = (Integer) objects[3];
        this.skill = (Double) objects[4];
    }

    public String id;
    public String name;
    public String avatarImage;
    public int games;
    public double skill;

    @Override
    public String toString() {
        return "PlayerSkill{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", avatarImage='" + avatarImage + '\'' +
               ", skill=" + skill +
               '}';
    }
}
