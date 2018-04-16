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
 * 同步获取节点数据并监听节点变化，当节点数据发生改变时输出并继续监听
 * 输出：
 *   data : isClosed:false
     stat : 81604378650,81604378650,1523845370786,1523845370786,0,0,0,0,14,0,81604378650

     [zk: localhost:2181(CONNECTED) 2] set /api_node_3 isClosed:true -1

     path : /api_node_3节点数据发生改变
     data : isClosed:true
     stat : 81604378650,81604378652,1523845370786,1523845416308,1,0,0,0,13,0,81604378650
 */
public class GetDataWithSyncTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new GetDataWithSyncTest());
            countDownLatch.await();

            String path = zooKeeper.create("/api_node_3",
                    "isClosed:false".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
            Stat stat = new Stat();
            byte[] data = zooKeeper.getData(path, new GetDataWithSyncTest(), stat);
            System.out.println("data : " + new String(data));
            System.out.println("stat : " + stat);
            zooKeeper.setData("/api_node_3", "isClosed:true".getBytes(), -1);
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
            if (watchedEvent.getPath() == null && watchedEvent.getType() == Event.EventType.None) {
                countDownLatch.countDown();
            } else {
                switch (watchedEvent.getType()) {
                    case NodeDataChanged:
                        System.out.println("path : " + watchedEvent.getPath() + "节点数据发生改变");
                        Stat stat = new Stat();
                        byte[] data;
                        try {
                            data = zooKeeper.getData(watchedEvent.getPath(), new GetDataWithSyncTest(), stat);
                            System.out.println("data : " + new String(data));
                            System.out.println("stat : " + stat);
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
    }


}
