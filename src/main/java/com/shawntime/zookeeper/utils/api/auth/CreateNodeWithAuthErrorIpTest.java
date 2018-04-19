package com.shawntime.zookeeper.utils.api.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;

/**
 * 创建节点时权限模式为ip，设置查询、修改组合权限
 * 初始化设置允许的ip地址为127.0.0.2
 * 则查询和修改无权限
 */
public class CreateNodeWithAuthErrorIpTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new CreateNodeWithAuthErrorIpTest());
            countDownLatch.await();
            createNodeWithAuth();
            getNodeWithIpAuth();
            setNodeWithIpAuth();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * org.apache.zookeeper.KeeperException$NoAuthException: KeeperErrorCode = NoAuth for /create_node_with_auth_error_ip
     */
    private static void getNodeWithIpAuth() {
        Stat stat = new Stat();
        byte[] data = new byte[0];
        try {
            data = zooKeeper.getData("/create_node_with_auth_error_ip", true, stat);
            System.out.println("data : " + new String(data));
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * org.apache.zookeeper.KeeperException$NoAuthException: KeeperErrorCode = NoAuth for /create_node_with_auth_error_ip
     */
    private static void setNodeWithIpAuth() {
        try {
            zooKeeper.setData("/create_node_with_auth_error_ip", "nihao".getBytes(), -1);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 权限模式（scheme）：ip、digest
     * 授权对象（ID）：
     *  ip模式：具体的ip地址
     *  digest模式：username:Base64(SHA-1(username:password))
     * 权限（permission）：
     *  create(C), DELETE(D),READ(R), WRITE(W), ADMIN(A)
     *
     *  权限组合: scheme + ID + permission
     *
     */
    private static void createNodeWithAuth() {
        ACL acl = new ACL();
        acl.setPerms(ZooDefs.Perms.READ | ZooDefs.Perms.WRITE);
        acl.setId(new Id("ip", "127.0.0.2"));
        List<ACL> aclList = new ArrayList<ACL>();
        aclList.add(acl);
        try {
            zooKeeper.delete("/create_node_with_auth_error_ip", -1);
            String path = zooKeeper.create("/create_node_with_auth_error_ip",
                    "data".getBytes(),
                    aclList,
                    CreateMode.PERSISTENT);
            System.out.println("path : " + path + " created success...");
        } catch (KeeperException e) {
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
