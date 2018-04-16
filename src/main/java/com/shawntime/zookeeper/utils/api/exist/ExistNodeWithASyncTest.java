package com.shawntime.zookeeper.utils.api.exist;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * 异步判断节点是否存在
 * exist监听：nodeCreated、nodeDataChanged、NodeDeleted，无法监听NodeChildrenChanged
 * 输出：
 *   rc : 0
     path : /api_node
     rtx : 上下文
     stat : 81604378717,81604378717,1523868037545,1523868037545,0,36,0,0,0,0,85899345926
 */
public class ExistNodeWithASyncTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new ExistNodeWithASyncTest());
            countDownLatch.await();
            zooKeeper.exists("/api_node", new ExistNodeWithASyncTest(), new AsyncCallback.StatCallback() {
                public void processResult(int rc, String path, Object rtx, Stat stat) {
                    System.out.println("rc : " + rc); // rc=0存在
                    System.out.println("path : " + path);
                    System.out.println("rtx : " + rtx);
                    System.out.println("stat : " + stat);
                }
            }, "上下文");
            Thread.sleep(Integer.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
