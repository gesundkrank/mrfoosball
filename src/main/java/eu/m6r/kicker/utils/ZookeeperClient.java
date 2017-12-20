package eu.m6r.kicker.utils;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ZookeeperClient {
    public static String ZOOKEEPER_ROOT_PATH = "/kicker";

    private final ZooKeeper zooKeeper;

    public ZookeeperClient(final String zookeeperHosts) throws IOException {
        this.zooKeeper = new ZooKeeper(zookeeperHosts, 30000, null);
    }

    public void createPath(final String path)
            throws KeeperException, InterruptedException {
        StringBuilder currentPath = new StringBuilder();

        final List<String> paths =
                Arrays.stream(path.split("/")).filter(p -> !p.isEmpty())
                        .collect(Collectors.toList());

        for (String subPath : paths) {
            currentPath.append("/").append(subPath);
            if (zooKeeper.exists(currentPath.toString(), false) == null) {
                zooKeeper.create(currentPath.toString(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                 CreateMode.PERSISTENT);
            }
        }
    }

    public String createEphemeralSequential(final String path, final String value)
            throws KeeperException, InterruptedException {
        return zooKeeper.create(path, value.getBytes(),
                         ZooDefs.Ids.OPEN_ACL_UNSAFE,
                         CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public boolean checkLock(final String lockPath, final Watcher watcher)
            throws KeeperException, InterruptedException {
        final String parentPath = Paths.get(lockPath).getParent().toString();

        final List<String> children = zooKeeper.getChildren(parentPath, watcher);
        return children.stream()
                .min(String::compareTo)
                .map(string -> string.equals(lockPath))
                .orElse(false);
    }
}
