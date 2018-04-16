package com.shawntime.zookeeper.utils.api.data;

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
 * 同步修改节点数据
 * 输出：
 *   修改前
     data : 修改前的数据
     stat : 81604378699,81604378699,1523865072462,1523865072462,0,0,0,99868011290165248,18,0,81604378699

     修改后
     data : 修改后的数据
     stat : 81604378699,81604378700,1523865072462,1523865072702,1,0,0,99868011290165248,18,0,81604378699
 */
public class SetDataWithSyncTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new SetDataWithSyncTest());
            countDownLatch.await();
            String path = zooKeeper.create("/api_node",
                    "修改前的数据".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
            Stat stat = new Stat();
            byte[] data = zooKeeper.getData(path, new SetDataWithSyncTest(), stat);
            System.out.println("修改前");
            System.out.println("data : " + new String(data));
            System.out.println("stat : " + stat);

            zooKeeper.setData(path, "修改后的数据".getBytes(), stat.getVersion());
            data = zooKeeper.getData(path, new SetDataWithSyncTest(), stat);
            System.out.println("修改后");
            System.out.println("data : " + new String(data));
            System.out.println("stat : " + stat);

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
