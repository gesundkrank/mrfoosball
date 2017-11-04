package eu.m6r.kicker.models;

public class PlayerSkill {

    public PlayerSkill() {
    }

    public PlayerSkill(final Player player) {
        this.id = player.id;
        this.avatarImage = player.avatarImage;
        this.name = player.name;
        this.skill = player.trueSkillMean - 3 * player.trueSkillStandardDeviation;
    }

    public String id;
    public String name;
    public String avatarImage;
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
