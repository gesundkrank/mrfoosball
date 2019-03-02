package eu.m6r.kicker.models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Match {
    @Id
    @GeneratedValue
    public int id;
    public Date date = new Date();
    public int teamA = 0;
    public int teamB = 0;

    @Enumerated(EnumType.STRING)
    public State state = State.RUNNING;
}
