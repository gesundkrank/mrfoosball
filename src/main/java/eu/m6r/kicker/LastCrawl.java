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

package eu.m6r.kicker;

import java.io.IOException;

import eu.m6r.kicker.models.Crawl;
import eu.m6r.kicker.utils.JsonConverter;
import eu.m6r.kicker.utils.ZookeeperClient;

public class LastCrawl {
    private static final String CRAWL_PATH = "/kicker/lastCrawl";

    private final ZookeeperClient zookeeperClient;
    private final JsonConverter jsonConverter;

    public LastCrawl(final String zookeeperHosts)throws IOException {
        this.zookeeperClient = new ZookeeperClient(zookeeperHosts);
        zookeeperClient.createPath(CRAWL_PATH);
        this.jsonConverter = new JsonConverter(Crawl.class);
    }

    private String path(final String channelId) {
        return String.format("%s/%s", CRAWL_PATH, channelId);
    }

    public Crawl get(final String channelId)
            throws IOException, Controller.NoLastCrawlException {
        final String value = zookeeperClient.readNode(path(channelId));
        if (value == null || value.isEmpty()) {
            throw new Controller.NoLastCrawlException();
        }

        return jsonConverter.fromString(value, Crawl.class);
    }

    public void save(final Crawl crawl) throws IOException {
        final String crawlPath = path(crawl.channelId);
        zookeeperClient.writeNode(crawlPath, jsonConverter.toString(crawl));
    }
}
