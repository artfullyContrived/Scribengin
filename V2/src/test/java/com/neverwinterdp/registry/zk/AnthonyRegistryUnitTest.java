package com.neverwinterdp.registry.zk;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.scribengin.dependency.ZookeeperServerLauncher;
import com.neverwinterdp.util.FileUtil;

public class AnthonyRegistryUnitTest {
  static {
    System.setProperty("log4j.configuration", "file:src/test/resources/log4j.properties");
  }

  private ZookeeperServerLauncher zkServerLauncher;
  private CuratorFramework curator;
  private PathChildrenCache pathChildrenCache;

  @Before
  public void setup() throws Exception {
    FileUtil.removeIfExist("./build/data", false);

    zkServerLauncher = new ZookeeperServerLauncher("./build/data/zookeeper");
    zkServerLauncher.start();

    String zkHost = zkServerLauncher.getHost() + ":" + zkServerLauncher.getPort();
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    curator = CuratorFrameworkFactory.newClient(zkHost, retryPolicy);
    curator.start();
    curator.blockUntilConnected();
  }

  @After
  public void teardown() throws Exception {
    // pathChildrenCache.close();
    curator.close();
    zkServerLauncher.shutdown();
  }


  @Test
  public void testWatcher() throws Exception {
    String path = "/test";
    String data = "here's a string";

    // create node
    curator.create().creatingParentsIfNeeded().forPath(path);
    curator.setData().forPath(path, data.getBytes());

    assertEquals(data, new String(curator.getData().forPath(path)));

    AtomicInteger abSignal = new AtomicInteger();

    TopicNodeListener listener2 = new TopicNodeListener("/test", abSignal);
    setTopicNodeListener(listener2);

    curator.setData().forPath("/test/one", "hey there".getBytes());
    curator.delete().forPath("/test/one");
    curator.setData().forPath("/test/one", "Bonjour".getBytes());
    assertEquals(3, abSignal.get());

  }

  // Listener for node changes
  public void setTopicNodeListener(TopicNodeListener topicNodeListener) throws Exception {
    pathChildrenCache =
        new PathChildrenCache(curator, topicNodeListener.getNode(),
            true);
    pathChildrenCache.start(StartMode.BUILD_INITIAL_CACHE);
    pathChildrenCache.getListenable().addListener(topicNodeListener);
  }
}
