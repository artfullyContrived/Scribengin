package com.neverwinterdp.scribengin.source.kafka;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import com.neverwinterdp.scribengin.fixture.KafkaFixture;
import com.neverwinterdp.scribengin.fixture.ZookeeperFixture;
import com.neverwinterdp.scribengin.util.ZookeeperUtils;

public class Servers {

  private static final Logger logger = Logger.getLogger(Servers.class);
  private ZookeeperFixture zookeeperFixture;
  private ZookeeperUtils zkUtils;
  private int partitions;
  private int kafkaPort;
  private Set<KafkaFixture> kafkaBrokers;
  private String topic;
  private int zkPort;
  private String zkHost;
  private String version;
  private String kafkaHost;
  private static final Random rnd = new Random();



  public Servers(int partitions, int kafkaPort, int zkPort, String zkHost, String version,
      String kafkaHost, String topic) {
    super();
    this.partitions = partitions;
    this.kafkaPort = kafkaPort;
    this.zkPort = zkPort;
    this.zkHost = zkHost;
    this.version = version;
    this.kafkaHost = kafkaHost;
    this.topic = topic;
    kafkaBrokers = new HashSet<>();
  }


  public void start() throws Exception {
    createServers();
  }


  private void createServers() throws Exception {
    zookeeperFixture = new ZookeeperFixture(version, zkHost, zkPort);
    zookeeperFixture.start();

    zkUtils = new ZookeeperUtils(zkHost + ":" + zkPort);
    zkUtils.removeKafkaData();
    KafkaFixture kafkaFixture;
    for (int i = 0; i < partitions; i++) {
      kafkaFixture =
          new KafkaFixture(version, kafkaHost, kafkaPort + i, zkHost, zkPort, partitions);
      kafkaFixture.start();
      kafkaBrokers.add(kafkaFixture);
    }
    createKafkaData(1000);
  }

  public ZookeeperUtils getZkUtils() {
    return zkUtils;
  }

  public Set<KafkaFixture> getKafkaBrokers() {
    return kafkaBrokers;
  }

  private void createKafkaData(int startNum) {
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

  private String getBrokers() {
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

  public void stop() throws IOException {
    zookeeperFixture.stop();
    for (KafkaFixture kafkaFixture : kafkaBrokers) {
      kafkaFixture.stop();
    }
    zkUtils.close();
  }
}
