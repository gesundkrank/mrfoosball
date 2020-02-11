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

package de.gesundkrank.mrfoosball.api.binders;

import java.io.IOException;
import javax.ws.rs.ext.Provider;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import de.gesundkrank.mrfoosball.slack.Bot;

@Provider
public class BotBinder extends AbstractBinder {

    @Override
    protected void configure() {
        try {
            bind(new Bot()).to(Bot.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Bot", e);
        }
    }
}
