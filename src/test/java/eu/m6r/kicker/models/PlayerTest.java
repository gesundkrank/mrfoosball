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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class PlayerTest {

    @Test
    public void hashCodeShouldMatchForEqualId() {
        final var player1 = new Player();
        player1.id = "player1";
        player1.avatarImage = "image1";
        player1.name = "name1";

        final var player2 = new Player();
        player2.id = "player1";
        player2.avatarImage = "image2";
        player2.name = "name2";

        assertEquals(player1.hashCode(), player2.hashCode());
    }

    @Test
    public void hashCodeShouldNotMatchForUnequalId() {
        final var player1 = new Player();
        player1.id = "player1";
        player1.avatarImage = "image1";
        player1.name = "name1";

        final var player2 = new Player();
        player2.id = "player2";
        player2.avatarImage = "image2";
        player2.name = "name2";

        assertNotEquals(player1.hashCode(), player2.hashCode());
    }
}
