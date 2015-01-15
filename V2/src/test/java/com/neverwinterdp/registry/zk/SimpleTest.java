package com.neverwinterdp.registry.zk;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryOneTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.scribengin.dependency.ZookeeperServerLauncher;
import com.neverwinterdp.util.FileUtil;


public class SimpleTest
{
  private ZookeeperServerLauncher zkServerLauncher;

  // private CuratorFramework zkClient;

  @Before
  public void setup() throws Exception {
    FileUtil.removeIfExist("./build/data", false);

    zkServerLauncher = new ZookeeperServerLauncher("./build/data/zookeeper");
    zkServerLauncher.start();

    String zkHost = zkServerLauncher.getHost() + ":" + zkServerLauncher.getPort();
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    // zkClient = CuratorFrameworkFactory.newClient(zkHost, retryPolicy);
    // zkClient.start();
    // zkClient.blockUntilConnected();
  }

  @Test
  public void testAddNode_deleteNode_addNode_should_receiveThreeEvents() throws Exception
  {

    PathChildrenCache myCache = null;
    CuratorFramework myClient =
        CuratorFrameworkFactory.newClient(
            zkServerLauncher.getHost() + ":" + zkServerLauncher.getPort(), new RetryOneTime(1));
    try
    {
      myClient.start();

      final BlockingQueue<PathChildrenCacheEvent> myEvents =
          new LinkedBlockingQueue<PathChildrenCacheEvent>();

      myCache = new PathChildrenCache(myClient, "/test", true);
      myCache.getListenable().addListener
          (
              new PathChildrenCacheListener()
              {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
                    throws Exception
                {
                  System.out.println("kume happen");
                  myEvents.offer(event);
                }
              }
          );
      myCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

      myClient.create().forPath("/test");
      myClient.create().forPath("/test/one", "hey there".getBytes());
      // Thread.sleep(50);
      myClient.delete().forPath("/test/one");
      myClient.create().forPath("/test/one", "Bonjour".getBytes());

      PathChildrenCacheEvent myChildrenCacheEvent = myEvents.poll(10, TimeUnit.SECONDS);
      Assert.assertEquals(myChildrenCacheEvent.getType(), PathChildrenCacheEvent.Type.CHILD_ADDED);

      myChildrenCacheEvent = myEvents.poll(10, TimeUnit.SECONDS);
      Assert.assertEquals(myChildrenCacheEvent.getType(), PathChildrenCacheEvent.Type.INITIALIZED);

      myChildrenCacheEvent = myEvents.poll(10, TimeUnit.SECONDS);
      Assert
          .assertEquals(myChildrenCacheEvent.getType(), PathChildrenCacheEvent.Type.CHILD_REMOVED);

      myChildrenCacheEvent = myEvents.poll(10, TimeUnit.SECONDS);
      Assert.assertEquals(myChildrenCacheEvent.getType(), PathChildrenCacheEvent.Type.CHILD_ADDED);
    } finally
    {
      myCache.close();
      myClient.close();
    }
  }
}
