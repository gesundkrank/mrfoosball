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
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;

public class Lock extends ZookeeperClient {

    private final String lockPath;

    private String lockCreated;

    public Lock(final String zookeeperHosts, final String subDir) throws IOException {
        super(zookeeperHosts, subDir);
        this.lockPath = this.subDir + "/lock_";
    }

    /**
     * Creates a lock in Zookeeper once it successfully obtained the lock or in case of an error
     * the callback function is called.
     * @param callback Callback function that is called in case of an unrecoverable error or
     *                 success.
     *                 - In case of an error the parameter holds the exception.
     *                 - In case of success the parameter is null.
     */
    public void lock(final Callback callback) {
        try {
            if (lockCreated == null) {
                lockCreated = createEphemeralSequential(lockPath);
                logger.info("Created new lock entry {}", lockCreated);
            }

            checkLock(callback);
        } catch (InterruptedException | KeeperException exception) {
            callback.accept(exception);
        }
    }

    protected Optional<String> getCurrentLock()
            throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(subDir, null)
                .stream()
                .min(String::compareTo)
                .map(string -> {
                    logger.debug("Current min lock node: {}", string);
                    return String.format("%s/%s", subDir, string);
                });
    }

    private void checkLock(final Callback callback) {
        try {
            final var currentLockOption = getCurrentLock();

            if (currentLockOption.isEmpty()) {
                logger.warn("No lock has been created in Zookeeper. Retrying...");
                lock(callback);
                return;
            }

            final var currentLock = currentLockOption.get();

            if (currentLock.equals(lockCreated)) {
                logger.info("Obtained lock {}", currentLock);
                callback.accept(null);
                return;
            }

            final var exists = zooKeeper.exists(currentLock, new LockWatcher(callback));
            if (exists == null) {
                callback.accept(null);
            }
        } catch (InterruptedException | KeeperException exception) {
            callback.accept(exception);
        }
    }

    private String createEphemeralSequential(final String path)
            throws KeeperException, InterruptedException {
        return zooKeeper.create(path, "lock".getBytes(),
                                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    private class LockWatcher implements Watcher {

        private final Callback callback;

        public LockWatcher(final Callback callback) {
            this.callback = callback;
        }

        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeDeleted) {
                logger.debug("Min node {} has been deleted. Checking lock  again.",
                             event.getPath());
                checkLock(callback);
            }
        }
    }

    public interface Callback extends Consumer<Exception> {

    }
}
