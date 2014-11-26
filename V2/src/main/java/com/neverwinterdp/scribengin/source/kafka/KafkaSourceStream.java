package com.neverwinterdp.scribengin.source.kafka;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;

import com.neverwinterdp.scribengin.source.SourceStream;
import com.neverwinterdp.scribengin.source.SourceStreamDescriptor;
import com.neverwinterdp.scribengin.source.SourceStreamReader;
import com.neverwinterdp.scribengin.util.HostPort;

/**
 * The kafka source reads from zk how many partitions the topic has and starts sourceStreams for each partition
 * 
 * SourceStream has reader to read
 * */
public class KafkaSourceStream implements SourceStream {

  private KafkaSourceStreamDescriptor sourceStreamDescriptor;
  private KafkaSourceStreamReader sourceStreamReader;

  /*  private HostPort leader;
    private Set<HostPort> brokers;*/

  public KafkaSourceStream(KafkaSourceStreamDescriptor sourceStreamDescriptor) {
    this.sourceStreamDescriptor = sourceStreamDescriptor;
  }

  public SourceStreamDescriptor getSourceStreamDescriptor() {
    return sourceStreamDescriptor;
  }

  public void setSourceStreamDescriptor(KafkaSourceStreamDescriptor sourceStreamDescriptor) {
    this.sourceStreamDescriptor = sourceStreamDescriptor;
  }

  public SourceStreamReader getSourceStreamReader() {
    return sourceStreamReader;
  }

  public void setSourceStreamReader(KafkaSourceStreamReader sourceStreamReader) {
    this.sourceStreamReader = sourceStreamReader;
  }

  public HostPort getLeader() {
    // From descriptor get leader of brokers.

    return findLeader(sourceStreamDescriptor.getTopic(), sourceStreamDescriptor.getId());
  }

  private HostPort findLeader(String topic, int partition) {
    logger.info("findLeader. Topic:" + topic + " Partition: " + partition);
    PartitionMetadata partitionMetaData = null;
    loop: for (HostPort hostPort : sourceStreamDescriptor.getBrokers()) {
      SimpleConsumer consumer = null;
      try {
        consumer =
            new SimpleConsumer(hostPort.getHost(), hostPort.getPort(), 100000, 64 * 1024,
                "leaderLookup");
        List<String> topics = Collections.singletonList(topic);
        TopicMetadataRequest req = new TopicMetadataRequest(topics);
        kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);

        List<TopicMetadata> metaData = resp.topicsMetadata();
        for (TopicMetadata item : metaData) {
          for (PartitionMetadata part : item.partitionsMetadata()) {
            if (part.partitionId() == partition) {
              partitionMetaData = part;
              break loop;
            }
          }
        }
      } catch (Exception e) {
        System.out.println("Error communicating with Broker [" + hostPort.getHost() + ":"
            + hostPort.getPort() + "] to find Leader for [" + topic
            + ", " + partition + "] Reason: " + e);
      } finally {
        if (consumer != null)
          consumer.close();
      }
    }

    return new HostPort(partitionMetaData.leader().host(), partitionMetaData.leader().port());
  }

  public Set<HostPort> getBrokers() {
    return new HashSet<HostPort>(sourceStreamDescriptor.getBrokers());
  }

  @Override
  public SourceStreamDescriptor getDescriptor() {
    return sourceStreamDescriptor;
  }

  @Override
  public SourceStreamReader getReader(String name) {
    return sourceStreamReader;
  }



  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((sourceStreamDescriptor == null) ? 0 : sourceStreamDescriptor.hashCode());
    result = prime * result + ((sourceStreamReader == null) ? 0 : sourceStreamReader.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    KafkaSourceStream other = (KafkaSourceStream) obj;
    if (sourceStreamDescriptor == null) {
      if (other.sourceStreamDescriptor != null)
        return false;
    } else if (!sourceStreamDescriptor.equals(other.sourceStreamDescriptor))
      return false;
    if (sourceStreamReader == null) {
      if (other.sourceStreamReader != null)
        return false;
    } else if (!sourceStreamReader.equals(other.sourceStreamReader))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "KafkaSourceStream [sourceStreamDescriptor=" + sourceStreamDescriptor
        + ", sourceStreamReader=" + sourceStreamReader + "]";
  }

}
