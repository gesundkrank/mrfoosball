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

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TeamTest {

    @Test
    public void hashCodeShouldMatchForEqualTeams() {
        final var player1 = new Player("player1", "name1", "image1");
        final var player2 = new Player("player2", "name1", "image1");
        final var teamA = new Team(player1, player2);
        final var teamB = new Team(player1, player2);

        assertEquals(teamA, teamB);
    }

    @Test
    public void hashCodeShouldNotMatchForUnequalTeams() {
        final var player1 = new Player("player1", "name1", "image1");
        final var player2 = new Player("player2", "name1", "image1");
        final var teamA = new Team(player1, player2);
        final var teamB = new Team(player2, player1);

        assertNotEquals(teamA, teamB);
    }
}
