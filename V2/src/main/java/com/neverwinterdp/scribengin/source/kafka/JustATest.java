package com.neverwinterdp.scribengin.source.kafka;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;
import com.neverwinterdp.scribengin.fixture.KafkaFixture;
import com.neverwinterdp.scribengin.fixture.ZookeeperFixture;
import com.neverwinterdp.scribengin.source.SourceDescriptor;



public class JustATest {

  private static final Logger logger = Logger.getLogger(JustATest.class);
  private static ZookeeperFixture zookeeperFixture;
  private static HashSet<KafkaFixture> kafkaBrokers;
  private static String topic = "scribe";
  private static int partitions=2;

  public static void main(String[] args) throws IOException, InterruptedException {
    try {
      init();
      createKafkaData(100);
      SourceDescriptor descriptor = new SourceDescriptor();
      descriptor.setName(topic);
      descriptor.setLocation("127.0.0.1:2181");
      KafkaSource source = new KafkaSource(descriptor);

      Arrays.toString(source.getSourceStreams());

      source.close();
    } finally {
      stop();

    }
  }

  private static void stop() throws InterruptedException, IOException {
    Thread.sleep(1000);
    zookeeperFixture.stop();

    for (KafkaFixture kafkaFix : kafkaBrokers) {
      kafkaFix.stop();
    }

  }



  private static void init() throws IOException {
    zookeeperFixture = new ZookeeperFixture("0.8.1", "127.0.0.1", 2181);
    zookeeperFixture.start();

    kafkaBrokers = Sets.newHashSet();
    KafkaFixture kafkaFixture;
    for (int i = 0; i < 2; i++) {
      int kafkaPort = 9092;
      kafkaFixture = new KafkaFixture("0.8.1", "127.0.0.1", kafkaPort + i, "127.0.0.1", 2181, partitions);
      kafkaFixture.start();

      kafkaBrokers.add(kafkaFixture);
    }
  }

  private static void createKafkaData(int startNum) {
    Random rnd = new Random();
    logger.info("createKafkaData. " + startNum);
    long events = Long.parseLong(startNum + "");
    Properties props = new Properties();
    props.put("metadata.broker.list", "127.0.0.1:9092, 127.0.0.1:9093");
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
}
