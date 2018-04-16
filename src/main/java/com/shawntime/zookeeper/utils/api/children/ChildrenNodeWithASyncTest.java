package com.shawntime.zookeeper.utils.api.children;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

/**
 * 异步获取子节点列表并监听子节点变化
 *
 * 总结：子节点监听，子节点增加、删除可监听，子节点数据更新不监听
 *
 * 输出：
 *   rc : 0
     path : /api_node
     rtx : 上下文
     childList : [node_1, node_2, node_3]
     /api_node子节点发生改变
     children : [node_1, node_2, node_3, node_4]
     /api_node子节点发生改变
     children : [node_1, node_2, node_4]
 */
public class ChildrenNodeWithASyncTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new ChildrenNodeWithASyncTest());
            countDownLatch.await();
            zooKeeper.create("/api_node/node_1", "node_1".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            zooKeeper.create("/api_node/node_2", "node_2".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            zooKeeper.create("/api_node/node_3", "node_3".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            zooKeeper.getChildren("/api_node", true, new AsyncCallback.ChildrenCallback() {
                public void processResult(int rc, String path, Object rtx, List<String> childList) {
                    System.out.println("rc : " + rc);
                    System.out.println("path : " + path);
                    System.out.println("rtx : " + rtx);
                    System.out.println("childList : " + childList);
                }
            }, "上下文");
            zooKeeper.create("/api_node/node_4", "node_4".getBytes(),  // 监听有效
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            zooKeeper.delete("/api_node/node_3", -1); // 监听有效

            zooKeeper.setData("/api_node/node_4", "".getBytes(), -1); // 监听无效

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
                    case NodeChildrenChanged:
                        System.out.println(watchedEvent.getPath() + "子节点发生改变");
                        List<String> children = null;
                        try {
                            children = zooKeeper.getChildren("/api_node", true);
                            System.out.println("children : " + children);
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
