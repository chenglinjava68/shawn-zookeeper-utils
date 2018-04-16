package com.shawntime.zookeeper.utils.api.exist;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * 同步判断节点是否存在
 * exist监听：nodeCreated、nodeDataChanged、NodeDeleted，无法监听NodeChildrenChanged
 * 输出：
 *   null
     /api_node/node_exist NodeCreated...
     /api_node/node_exist NodeDataChanged...
     /api_node/node_exist NodeDeleted...
 */
public class ExistNodeWithSyncTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new ExistNodeWithSyncTest());
            countDownLatch.await();
            Stat stat = zooKeeper.exists("/api_node/node_exist", new ExistNodeWithSyncTest());
            System.out.println(stat);
            zooKeeper.create("/api_node/node_exist", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            zooKeeper.setData("/api_node/node_exist", "data".getBytes(), -1);
            zooKeeper.create("/api_node/node_exist/node_1", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode
                    .EPHEMERAL);
            zooKeeper.delete("/api_node/node_exist/node_1", -1);
            zooKeeper.delete("/api_node/node_exist", -1);
            Thread.sleep(Integer.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            if (watchedEvent.getType() == Event.EventType.None && watchedEvent.getPath() == null) {
                countDownLatch.countDown();
            } else {
                try {
                    switch (watchedEvent.getType()) {
                        case NodeCreated:
                            System.out.println(watchedEvent.getPath() + " NodeCreated...");
                            zooKeeper.exists("/api_node/node_exist", new ExistNodeWithSyncTest());
                            break;
                        case NodeDataChanged:
                            System.out.println(watchedEvent.getPath() + " NodeDataChanged...");
                            zooKeeper.exists("/api_node/node_exist", new ExistNodeWithSyncTest());
                            break;
                        case NodeDeleted:
                            System.out.println(watchedEvent.getPath() + " NodeDeleted...");
                            zooKeeper.exists("/api_node/node_exist", new ExistNodeWithSyncTest());
                            break;
                        case NodeChildrenChanged:
                            System.out.println(watchedEvent.getPath() + " NodeChildrenChanged...");
                            zooKeeper.exists("/api_node/node_exist", new ExistNodeWithSyncTest());
                            break;
                    }

                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
