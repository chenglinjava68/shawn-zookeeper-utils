package com.shawntime.zookeeper.utils.api.delete;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * 异步删除节点
 * 输出：
 *   rc : 0
     path : /api_node/node_del
     rtx : 上下文
 */
public class DeleteNodeWithASyncTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new DeleteNodeWithASyncTest());
            countDownLatch.await();
            String path = zooKeeper.create("/api_node/node_del", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
            zooKeeper.delete(path, -1, new AsyncCallback.VoidCallback() {
                public void processResult(int rc, String path, Object rtx) {
                    System.out.println("rc : " + rc);
                    System.out.println("path : " + path);
                    System.out.println("rtx : " + rtx);
                }
            }, "上下文");
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
            }
        }
    }
}
