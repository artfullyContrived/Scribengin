package com.neverwinterdp.registry.zk;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

public class TopicNodeListener implements PathChildrenCacheListener {

  private String node;
  private AtomicInteger integer;

  public TopicNodeListener(String node, AtomicInteger integer) {
    System.out.println("hohoho");
    this.node = node;
    this.integer = integer;
  }

  public String getNode() {
    return node;
  }

  @Override
  public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
    System.out.println("hahaha" + event.getType());
    integer.incrementAndGet();
    System.out.println(integer);
  }
}
