package com.neverwinterdp.scribengin.source.kafka;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.neverwinterdp.scribengin.source.SourceDescriptor;
import com.neverwinterdp.scribengin.util.HostPort;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KafkaSourceTest {

  static {
    System.setProperty("log4j.configuration", "file:src/test/resources/log4j.properties");
  }

  private static final Logger logger = Logger.getLogger(KafkaSourceTest.class);


  private final static String topic = "scribe888";
  private static KafkaSource source;
  private static int partitions = 2;
  private static SourceDescriptor descriptor;
  private static int kafkaPort = 9092;
  private static String zkHost = "127.0.0.1";
  private static String kafkaHost = "127.0.0.1";
  private static String version = "0.8.1";
  private static int zkPort = 2181;

  static Servers servers;

  @BeforeClass
  public static void setup() throws Exception {
    logger.info("setup()");
    servers = new Servers(partitions, kafkaPort, zkPort, zkHost, version, kafkaHost, topic);
    servers.start();
  }


  @Test
  public void testGetSourceStreamInt() {
    logger.info("testGetSourceStreamInt. ");
    int partition = 0;
    descriptor = new SourceDescriptor();
    descriptor.setName(topic);
    descriptor.setLocation("127.0.0.1:2181");
    source = new KafkaSource(descriptor);
    Collection<HostPort> x = null;
    try {
      x = servers.getZkUtils().getBrokersForTopicAndPartition(topic, partition);
    } catch (Exception e) {
      e.printStackTrace();
    }
    KafkaSourceStreamDescriptor sourceStreamDescriptor =
        new KafkaSourceStreamDescriptor(topic, partition, x);
    assertEquals(sourceStreamDescriptor, source.getSourceStream(partition).getDescriptor());
  }

  @Test
  public void testUpdateSourceStreamsOnChangeOfPartitions() {
    //TODO test that on adding partitions source.getsourceStreams.length changes.
    descriptor = new SourceDescriptor();
    descriptor.setName(topic);
    descriptor.setLocation("127.0.0.1:2181");
    source = new KafkaSource(descriptor);
    int originalSourceStreamCount = source.getSourceStreams().length;
    int newPartitions = 2;
    servers.getZkUtils().addPartitions(topic, originalSourceStreamCount+newPartitions );
    int newPartitionCount = source.getSourceStreams().length;
    //Do we need to add kafka data here?


    assertEquals(originalSourceStreamCount + newPartitions, newPartitionCount);
  }

  @Test
  public void testGetSourceStreams() {
    logger.info("testGetSourceStreams. ");
    descriptor = new SourceDescriptor();
    descriptor.setName(topic);
    descriptor.setLocation("127.0.0.1:2181");
    source = new KafkaSource(descriptor);

    assertEquals(partitions, source.getSourceStreams().length);
  }



  public void restoreState() {
    descriptor = null;
    source = null;
  }

  @AfterClass
  public static void teardown() throws Exception {
    servers.stop();
  }
}
