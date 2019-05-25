/*
 * This file is part of kicker (https://github.com/mbrtargeting/kicker).
 * Copyright (c) 2019 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.m6r.kicker.models;

import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TournamentTest {

    @Test
    public void hashCodeShouldMatchForEqualTournaments() {
        final var playerA1 = new Player("player1", "name1", "image1");
        final var playerA2 = new Player("player2", "name1", "image1");
        final var playerB1 = new Player("player3", "name1", "image1");
        final var playerB2 = new Player("player4", "name1", "image1");
        final var teamA1 = new Team(playerA1, playerA2);
        final var teamB1 = new Team(playerB1, playerB2);

        final var dateMatch1 = new Date(0);
        final var match1 = new Match(dateMatch1, 6, 3, State.FINISHED);

        final var dateMatch2 = new Date(1);
        final var match2 = new Match(dateMatch2, 4, 6, State.FINISHED);

        final var dateMatch3 = new Date(2);
        final var match3 = new Match(dateMatch3, 4, 6, State.FINISHED);

        final var matches1 = List.of(match1, match2, match3);

        final var channel1 = new Channel("id", "slack", "name");

        final var tournament1 = new Tournament(3, dateMatch1, teamA1, teamB1, State.FINISHED, matches1, 0.1, 0.4, -0.1, -0.5, 0.2, -0.6, channel1);
        tournament1.id = 0;

        final var teamA2 = new Team(playerA1, playerA2);
        final var teamB2 = new Team(playerB1, playerB2);

        final var matches2 = List.of(match1, match2, match3);

        final var channel2 = new Channel("id", "slack", "name");

        final var tournament2 = new Tournament(3, dateMatch1, teamA2, teamB2, State.FINISHED, matches2, 0.1, 0.4, -0.1, -0.5, 0.2, -0.6, channel2);
        tournament2.id = 0;

        assertEquals(tournament1.hashCode(), tournament2.hashCode());
    }

}
