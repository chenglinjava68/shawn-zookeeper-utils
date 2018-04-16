package com.shawntime.zookeeper.utils.api.create;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

/**
 * 1-3：异步方式创建一个顺序持久节点
 * 输出：
 *   rc : 0
     path : /api_node_1
     ctx : 上下文
     realPath : /api_node_10000000013
 */
public class CreateNodeWithASyncTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new CreateNodeWithASyncTest());
            countDownLatch.await();
            // 连接成功
            zooKeeper.create("/api_node_1",
                    "123456".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT_SEQUENTIAL,
                    new AsyncCallback.StringCallback() {
                        /**
                         * rc : 返回状态码，0：成功，非0：失败
                         * path：创建的路径
                         * ctx：传入的上下文对象
                         * realPath：创建成功后服务端返回的路径
                         */
                        public void processResult(int rc, String path, Object ctx, String realPath) {
                            System.out.println(String.format("rc : %s", rc));
                            System.out.println(String.format("path : %s", path));
                            System.out.println(String.format("ctx : %s", ctx));
                            System.out.println(String.format("realPath : %s", realPath));
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
