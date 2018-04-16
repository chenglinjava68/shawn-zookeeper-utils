package com.shawntime.zookeeper.utils.api.data;

import static org.apache.zookeeper.AsyncCallback.*;

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
 * 异步获取节点数据并监听节点变化，当节点数据发生改变时输出并继续监听
 * 输出：
 *   rc : 0
     path : /api_node_3
     rtx : 上下文
     bytes : isClosed:false
     stat : 81604378684,81604378684,1523862469819,1523862469819,0,0,0,99855586678145042,14,0,81604378684

     path : /api_node_3节点数据发生改变
     data : isClosed:true
     stat : 81604378684,81604378685,1523862469819,1523862470008,1,0,0,99855586678145042,13,0,81604378684
 */
public class GetDataWithASyncTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new GetDataWithASyncTest());
            countDownLatch.await();

            String path = zooKeeper.create("/api_node_3",
                    "isClosed:false".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
            zooKeeper.getData(path, new GetDataWithASyncTest(),
                    new DataCallback() {
                        public void processResult(int rc, String path, Object rtx, byte[] bytes, Stat stat) {
                            System.out.println("rc : " + rc);
                            System.out.println("path : " + path);
                            System.out.println("rtx : " + rtx);
                            System.out.println("bytes : " + new String(bytes));
                            System.out.println("stat : " + stat);
                        }
                    }, "上下文");
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
                            data = zooKeeper.getData(watchedEvent.getPath(), new GetDataWithASyncTest(), stat);
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
