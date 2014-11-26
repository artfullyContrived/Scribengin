package com.neverwinterdp.scribengin.source.kafka;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.common.collect.Sets;
import com.neverwinterdp.scribengin.fixture.KafkaFixture;
import com.neverwinterdp.scribengin.fixture.ZookeeperFixture;
import com.neverwinterdp.scribengin.source.SourceDescriptor;
import com.neverwinterdp.scribengin.util.HostPort;
import com.neverwinterdp.scribengin.util.ZookeeperUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KafkaSourceTest {

  static {
    System.setProperty("log4j.configuration", "file:src/test/resources/log4j.properties");
  }

  private static final Logger logger = Logger.getLogger(KafkaSourceTest.class);
  private static ZookeeperUtils zkUtils;
  private static ZookeeperFixture zookeeperFixture;
  private static Set<KafkaFixture> kafkaBrokers;
  private final static String topic = "scribe888";
  private static KafkaSource source;
  private static int partitions = 2;
  private static SourceDescriptor descriptor;
  private static int kafkaPort = 9092;
  private static final Random rnd = new Random();

  @BeforeClass
  public static void setup() throws Exception {
    logger.info("setup()");
    kafkaBrokers = Sets.newLinkedHashSet();
    createServers();
    try {
      createKafkaData(100);
    } catch (Exception e) {

    }
  }

  private static void createServers() throws Exception {
    zookeeperFixture = new ZookeeperFixture("0.8.1", "127.0.0.1", 2181);
    zookeeperFixture.start();

    zkUtils = new ZookeeperUtils("127.0.0.1:2181");
    zkUtils.removeKafkaData();
    KafkaFixture kafkaFixture;
    for (int i = 0; i < partitions; i++) {
      kafkaFixture =
          new KafkaFixture("0.8.1", "127.0.0.1", kafkaPort + i, "127.0.0.1", 2181, partitions);
      kafkaFixture.start();
      kafkaBrokers.add(kafkaFixture);
    }
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
      x = zkUtils.getBrokersForTopicAndPartition(topic, partition);
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
    zkUtils.addPartitions(topic, newPartitions + originalSourceStreamCount);
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

  private static void createKafkaData(int startNum) {
    logger.info("createKafkaData. " + startNum);

    long events = Long.parseLong(startNum + "");
    Properties props = new Properties();
    props.put("metadata.broker.list", getBrokers());
    props.put("num.partitions", Integer.toString(partitions));
    props.put("serializer.class", "kafka.serializer.StringEncoder");
    props.put("partitioner.class", "com.neverwinterdp.scribengin.fixture.SimplePartitioner");
    props.put("request.required.acks", "0");

    ProducerConfig config = new ProducerConfig(props);

    Producer<String, String> producer = new Producer<String, String>(config);

    for (long nEvents = 0; nEvents < events; nEvents++) {
      long runtime = new Date().getTime();
      String ip = "192.168.2." + rnd.nextInt(255);
      String msg = runtime + ",www.example.com," + ip;
      KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic, ip, msg);
      producer.send(data);
    }
    producer.close();
  }

  private static String getBrokers() {
    StringBuilder builder = new StringBuilder();
    for (KafkaFixture kafkaFixture : kafkaBrokers) {
      builder.append(kafkaFixture.getHost());
      builder.append(':');
      builder.append(kafkaFixture.getPort());
      builder.append(',');
    }
    builder.setLength(builder.length() - 1);
    return builder.toString();
  }

  public void restoreState() {
    descriptor = null;
    source = null;
  }

  @AfterClass
  public static void teardown() throws Exception {
    for (KafkaFixture kafkaFixture : kafkaBrokers) {
      kafkaFixture.stop();
    }
    zkUtils.removeKafkaData();
    zkUtils.close();
    zookeeperFixture.stop();
  }
}
