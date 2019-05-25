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

public class MatchTest {

    @Test
    public void hashCodeShouldMatchForEqualMatches() {
        final var date = new Date();
        final var match1 = new Match(date, 6, 1, State.RUNNING);
        final var match2 = new Match(date, 6, 1, State.RUNNING);
        assertEquals(match1.hashCode(), match2.hashCode());
    }

    @Test
    public void hashCodeShouldNotMatchForUnequalMatches() {
        final var date = new Date(0);
        final var match1 = new Match(date, 6, 1, State.RUNNING);
        final var match2 = new Match(new Date(1), 6, 1, State.RUNNING);
        assertNotEquals(match1.hashCode(), match2.hashCode());

        final var match3 = new Match(date, 5, 1, State.RUNNING);
        assertNotEquals(match1.hashCode(), match3.hashCode());

        final var match4 = new Match(date, 6, 2, State.RUNNING);
        assertNotEquals(match1.hashCode(), match4.hashCode());

        final var match5 = new Match(date, 6, 1, State.FINISHED);
        assertNotEquals(match1.hashCode(), match5.hashCode());
    }

    @Test
    public void hashCodeShouldMatchForEqualListOfMatches() {
        final var date = new Date(0);
        final var match1 = new Match(date, 6, 1, State.RUNNING);
        final var match2 = new Match(new Date(1), 6, 1, State.RUNNING);
        final var match3 = new Match(date, 5, 1, State.RUNNING);

        final var matches1 = List.of(match1, match2, match3);
        final var matches2 = List.of(match1, match2, match3);

        assertEquals(matches1.hashCode(), matches2.hashCode());
    }
}
