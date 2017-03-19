package eu.m6r.kicker.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Player implements Comparable<Player> {

    @Id
    public String id;
    public String name;
    public String avatarImage;


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
        return String.format("id=%s, name=%s, avatarImage=%s", id, name, avatarImage);
    }

    @Override
    public int compareTo(Player o) {
        return id.compareTo(o.id);
    }
}
