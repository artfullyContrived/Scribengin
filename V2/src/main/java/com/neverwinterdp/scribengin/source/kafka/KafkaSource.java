package com.neverwinterdp.scribengin.source.kafka;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.neverwinterdp.scribengin.source.Source;
import com.neverwinterdp.scribengin.source.SourceDescriptor;
import com.neverwinterdp.scribengin.source.SourceStream;
import com.neverwinterdp.scribengin.source.SourceStreamDescriptor;
import com.neverwinterdp.scribengin.util.HostPort;
import com.neverwinterdp.scribengin.util.ZookeeperUtils;

// worry about reconnection, get partitions
public class KafkaSource extends TopicNodeListener implements Source, Closeable {
  //TODO: implement kafka source and assign each kafka partition as a source stream
  //TODO KafkaSourceDescriptor  for topic metadata?
  //Source descriptor defines topic?

  private static final Logger logger = Logger.getLogger(KafkaSource.class);

  private ZookeeperUtils zkUtils;
  private SourceDescriptor descriptor;
  private Set<SourceStream> sourceStreams;

  public KafkaSource(SourceDescriptor descriptor) {
    logger.info("KafkaSource. " + descriptor);
    this.descriptor = descriptor;
    initialize();
  }

  private void initialize() {
    logger.info("initialize. ");
    try {
      zkUtils = new ZookeeperUtils(descriptor.getLocation());
      sourceStreams = updateSourceStreamsSet();
      setAutoUpdate();
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  @Override
  public SourceStream getSourceStream(int id) {
    // id is partition?
    // TODO how ensure sourceStreams is populated? call initialize()?
    return Iterables.getOnlyElement(Collections2.filter(sourceStreams, new IdPredicate(id)));
  }

  @Override
  public SourceStream getSourceStream(SourceStreamDescriptor descriptor) {
    // TODO how ensure sourceStreams is populated call initialize()?
    return Iterables.getOnlyElement(Collections2.filter(sourceStreams, new DescriptorPredicate(
        descriptor)));
  }

  //TODO add watcher
  @Override
  public SourceStream[] getSourceStreams() {
    logger.info("getSourceStreams. ");
    sourceStreams = updateSourceStreamsSet();
    return sourceStreams.toArray(new SourceStream[sourceStreams.size()]);
  }

  /**
   * TODO get a better name
   * @return 
   */
  private Set<SourceStream> updateSourceStreamsSet() {
    logger.info("updateSourceStreamsSet. ");
    Set<SourceStream> sources = Sets.newHashSet();

    //kafka stores partitions as a String not an int
    //one partition, many brokers
    Multimap<Integer, HostPort> partitions = null;
    //get partitions for Topic
    try {
      logger.debug("zkUtils is null? " + (zkUtils == null));
      partitions = zkUtils.getBrokersForTopic(descriptor.getName());
      logger.info("NUMBER " + partitions);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    //Create SourceStreams here
    KafkaSourceStreamDescriptor sourceStreamDescriptor;
    KafkaSourceStreamReader sourceStreamReader;
    KafkaSourceStream sourceStream;

    for (Entry<Integer, Collection<HostPort>> partition : partitions.asMap().entrySet()) {
      sourceStreamDescriptor =
          new KafkaSourceStreamDescriptor(descriptor.getName(), partition.getKey(),
              partition.getValue());
      sourceStream = new KafkaSourceStream(sourceStreamDescriptor);
      sourceStreamReader = new KafkaSourceStreamReader(descriptor.getName(), sourceStream);
      sourceStream.setSourceStreamReader(sourceStreamReader);
      sources.add(sourceStream);
    }
    return sources;
  }

  @Override
  public SourceDescriptor getSourceDescriptor() {
    return descriptor;
  }

  public void setAutoUpdate() throws Exception {
    logger.info("setAutoUpdate. ");
    zkUtils.setTopicNodeListener(this);
  }

  @Override
  /**
   * Whenever the topic node in ZK is changed we update The sourceStreams.
   * TODO make good equals hash code so that update doesn't create new ones.
   * */
  public void update() {
    sourceStreams = updateSourceStreamsSet();
  }

  @Override
  public void close() throws IOException {
    zkUtils.close();
  }

  @Override
  public String getTopic() {
    return descriptor.getName();
  }
}


class IdPredicate implements Predicate<SourceStream> {

  private int id;

  public IdPredicate(int id) {
    this.id = id;
  }

  @Override
  public boolean apply(SourceStream input) {
    return input.getDescriptor().getId() == id;
  }
}


class DescriptorPredicate implements Predicate<SourceStream> {

  private SourceStreamDescriptor descriptor;

  public DescriptorPredicate(SourceStreamDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  @Override
  public boolean apply(SourceStream input) {
    return input.getDescriptor().equals(descriptor);
  }
}
