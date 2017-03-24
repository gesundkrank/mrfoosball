package eu.m6r.kicker.models;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table
@IdClass(Team.class)
public class Team implements Serializable {

    @Id
    @ManyToOne(cascade = CascadeType.PERSIST)
    public Player player1;

    @Id
    @ManyToOne(cascade = CascadeType.PERSIST)
    public Player player2;

    @Override
    public String toString() {
        return String.format("player1=%s, player2=%s", player1, player2);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Team) {
            final Team oTeam = (Team) obj;
            return oTeam.player1.equals(player1) && oTeam.player2.equals(player2);
        }
        return false;
    }
}
