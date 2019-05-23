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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Crawl {
    public String channelId;
    public List<String> winners;
    public List<String> losers;
    public Date timestamp;

    public Crawl() {}

    public Crawl(String channelId, Team winners, Team losers) {
        this.channelId = channelId;
        this.winners = Arrays.asList(winners.player1.name, winners.player2.name);
        this.losers = Arrays.asList(losers.player1.name, losers.player2.name);
        this.timestamp = new Date();
    }
}
