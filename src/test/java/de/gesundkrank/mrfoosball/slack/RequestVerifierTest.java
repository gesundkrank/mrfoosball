/*
 * This file is part of MrFoosball (https://github.com/gesundkrank/mrfoosball).
 * Copyright (c) 2020 Jan Graßegger.
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

package de.gesundkrank.mrfoosball.slack;

import java.security.GeneralSecurityException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RequestVerifierTest {

    @Test
    public void testVerify() throws GeneralSecurityException {
        final var body = ("token=xyzz0WbapA4vBCDEFasx0q6G&team_id=T1DC2JH3J&team_domain=testteamnow"
                          + "&channel_id=G8PSS9T3V&channel_name=foobar&user_id=U2CERLKJA&user_name="
                          + "roadrunner&command=%2Fwebhook-collect&text=&response_url=https%3A%2F%2"
                          + "Fhooks.slack.com%2Fcommands%2FT1DC2JH3J%2F397700885554%2F96rGlfmibIGlg"
                          + "cZRskXaIFfN&trigger_id=398738663015.47445629121.803a0bc887a14d10d2c447"
                          + "fce8b6703c");
        final var timestamp = "1531420618";
        final var secret = "8f742231b10e8888abcd99yyyzzz85a5";
        final var expected = "v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503";
        final var requestVerifier = new RequestVerifier(secret, Long.MAX_VALUE);
        Assert.assertTrue(requestVerifier.verify(expected, body, timestamp));
    }
}
