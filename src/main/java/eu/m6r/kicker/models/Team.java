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
public class Team implements Serializable{

    @Id
    @ManyToOne(cascade = CascadeType.PERSIST)
    public User player1;

    @Id
    @ManyToOne(cascade = CascadeType.PERSIST)
    public User player2;

    public String name;

    @Override
    public String toString() {
        return String.format("player1=%s, player2=%s", player1, player2);
    }
}
