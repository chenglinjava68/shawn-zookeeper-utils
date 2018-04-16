package com.shawntime.zookeeper.utils.api.data;

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
 * 异步修改节点数据
 * 输出：
 *   修改前
     data : 修改前的数据
     stat : 81604378703,81604378703,1523865289682,1523865289682,0,0,0,99868011290165249,18,0,81604378703

     修改成功
     rc : 0
     path : /api_node
     ctx : 上下文
     stat : 81604378703,81604378704,1523865289682,1523865289969,1,0,0,99868011290165249,18,0,81604378703
 */
public class SetDataWithASyncTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new SetDataWithASyncTest());
            countDownLatch.await();
            String path = zooKeeper.create("/api_node",
                    "修改前的数据".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
            Stat stat = new Stat();
            byte[] data = zooKeeper.getData(path, new SetDataWithASyncTest(), stat);
            System.out.println("修改前");
            System.out.println("data : " + new String(data));
            System.out.println("stat : " + stat);
            zooKeeper.setData(path,
                    "修改后的数据".getBytes(), stat.getVersion(),
                    new AsyncCallback.StatCallback() {
                        public void processResult(int rc, String path, Object ctx, Stat stat) {
                            System.out.println("修改成功");
                            System.out.println("rc : " + rc);
                            System.out.println("path : " + path);
                            System.out.println("ctx : " + ctx);
                            System.out.println("stat : " + stat);
                        }
                    },
                    "上下文");
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
