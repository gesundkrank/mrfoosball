package eu.m6r.kicker.models;

import eu.m6r.kicker.trueskill.TrueSkillCalculator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQueries({
 @NamedQuery(
         name = "get_players_ordered_by_skill",
         query = "from Player order by (trueSkillMean - 3 * trueSkillStandardDeviation) desc"
 )
})
@Entity
@Table
public class Player implements Comparable<Player> {

    @Id
    public String id;
    public String name;
    public String avatarImage;
    public Double trueSkillMean = TrueSkillCalculator.DEFAULT_INITIAL_MEAN;
    public Double trueSkillStandardDeviation =
            TrueSkillCalculator.DEFAULT_INITIAL_STANDARD_DEVIATION;


    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Player && ((Player) obj).id.equals(id);
    }

    @Override
    public String toString() {
        return "Player{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", avatarImage='" + avatarImage + '\'' +
               ", trueSkillMean=" + trueSkillMean +
               ", trueSkillStandardDeviation=" + trueSkillStandardDeviation +
               '}';
    }

    @Override
    public int compareTo(Player o) {
        return id.compareTo(o.id);
    }
}
