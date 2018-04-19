package com.shawntime.zookeeper.utils.api.auth;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

/**
 * 创建节点添加权限
 * 通过权限模式（scheme）：digest设置 password:123456
 */
public class CreateNodeWithAuthErrorDigestTest implements Watcher {

    private static ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new CreateNodeWithAuthErrorDigestTest());
            countDownLatch.await();
            createNodeWithAuth();
            getNodeWithDigestAuth();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * path : /api_create created success...
        org.apache.zookeeper.KeeperException$NoAuthException: KeeperErrorCode = NoAuth for /api_create
     */
    private static void getNodeWithDigestAuth() {
        zooKeeper.addAuthInfo("digest", "password:123".getBytes());
        Stat stat = new Stat();
        try {
            byte[] data = zooKeeper.getData("/api_create", true, stat);
            System.out.println("data : " + new String(data));
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
	 * 权限模式(scheme): ip, digest
	 * 授权对象(ID)
	 * 		ip权限模式:  具体的ip地址
	 * 		digest权限模式: username:Base64(SHA-1(username:password))
	 * 权限(permission): create(C), DELETE(D),READ(R), WRITE(W), ADMIN(A)
	 * 		注：单个权限，完全权限，复合权限
	 *
	 * 权限组合: scheme + ID + permission
	 *
	 * */
    private static void createNodeWithAuth() throws NoSuchAlgorithmException, KeeperException, InterruptedException {
        ACL digestAcl = new ACL(ZooDefs.Perms.CREATE|ZooDefs.Perms.READ, new Id("digest",
                DigestAuthenticationProvider.generateDigest("password:123456")));
        List<ACL> aclList = new ArrayList<ACL>();
        aclList.add(digestAcl);
        zooKeeper.delete("/api_create", -1);
        String path = zooKeeper.create("/api_create", "api信息".getBytes(), aclList, CreateMode.PERSISTENT);
        System.out.println("path : " + path + " created success...");
    }

    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            if (watchedEvent.getType() == Event.EventType.None && watchedEvent.getPath() == null) {
                countDownLatch.countDown();
            }
        }
    }
}
