package com.neverwinterdp.scribengin.source.kafka;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.log4j.Logger;

public abstract class TopicNodeListener implements PathChildrenCacheListener {

  protected static final Logger logger = Logger.getLogger(TopicNodeListener.class);

  @Override
  public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
    logger.info("HAHAHA " + event.getType() + " " + event.getData().getPath());
    update();
  }

  public abstract void update();

  public abstract String getTopic();
}
