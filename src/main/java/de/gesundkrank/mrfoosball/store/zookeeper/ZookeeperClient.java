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

package de.gesundkrank.mrfoosball.store.zookeeper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import de.gesundkrank.mrfoosball.utils.Properties;

public class ZookeeperClient {

    protected final Logger logger;
    protected final ZooKeeper zooKeeper;
    protected final String subDir;

    protected ZookeeperClient(final String zookeeperHosts, final String subDir) throws IOException {
        this.logger = LogManager.getLogger();
        final var watcher = new StubWatcher();
        this.zooKeeper = new ZooKeeper(zookeeperHosts, 30000, watcher);
        final var rootPath = Properties.getInstance().getZookeeperRootPath();
        this.subDir = String.format("%s/%s", rootPath, subDir);

        createPath(this.subDir);
    }

    protected void createPath(final String path) throws IOException {
        try {
            StringBuilder currentPath = new StringBuilder();

            final List<String> paths =
                    Arrays.stream(path.split("/")).filter(p -> !p.isEmpty())
                            .collect(Collectors.toList());

            for (String subPath : paths) {
                currentPath.append("/").append(subPath);

                if (zooKeeper.exists(currentPath.toString(), false) == null) {
                    logger.debug("Creating path {}", currentPath.toString());
                    zooKeeper.create(currentPath.toString(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                     CreateMode.PERSISTENT);
                }
            }
        } catch (InterruptedException | KeeperException e) {
            throw new IOException(e);
        }
    }

    protected void writeNode(final String path, final String value) throws IOException {
        try {
            final Stat stat = zooKeeper.exists(path, false);

            if (stat == null) {
                zooKeeper.create(path, value.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                 CreateMode.PERSISTENT);
            } else {
                zooKeeper.setData(path, value.getBytes(), stat.getVersion());
            }
        } catch (InterruptedException | KeeperException e) {
            throw new IOException(e);
        }
    }

    protected void deleteNode(final String path) throws IOException {
        try {
            final Stat stat = zooKeeper.exists(path, false);
            if (stat != null) {
                zooKeeper.delete(path, stat.getVersion());
            }
        } catch (InterruptedException | KeeperException e) {
            throw new IOException(e);
        }
    }

    protected String readNode(final String path) throws IOException {
        try {
            if (zooKeeper.exists(path, false) == null) {
                return null;
            }

            return new String(zooKeeper.getData(path, null, null));
        } catch (InterruptedException | KeeperException e) {
            throw new IOException(e);
        }
    }

    public static class StubWatcher implements Watcher {

        private final Logger logger;

        public StubWatcher() {
            logger = LogManager.getLogger();
        }

        @Override
        public void process(WatchedEvent event) {
            logger.debug(event);
        }
    }
}
