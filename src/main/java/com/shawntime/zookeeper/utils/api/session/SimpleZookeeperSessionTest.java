package com.shawntime.zookeeper.utils.api.session;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * 1-1：创建一个简单的zookeeper会话
 * 输出：
 *  CONNECTING
    连接成功....
    CONNECTED
 */
public class SimpleZookeeperSessionTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new SimpleZookeeperSessionTest());
            System.out.println(zooKeeper.getState());
            countDownLatch.await();
            System.out.println(zooKeeper.getState());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            if (watchedEvent.getType() == Event.EventType.None && watchedEvent.getPath() == null) {
                System.out.println("连接成功....");
                countDownLatch.countDown();
            }
        }
    }
}
