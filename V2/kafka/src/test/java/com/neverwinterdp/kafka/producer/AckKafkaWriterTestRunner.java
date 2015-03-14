package com.neverwinterdp.kafka.producer;

import java.util.Set;

import kafka.cluster.Broker;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;

import com.neverwinterdp.kafka.tool.KafkaTool;
import com.neverwinterdp.kafka.tool.KafkaTopicCheckTool;
import com.neverwinterdp.kafka.tool.KafkaTopicConfig;
import com.neverwinterdp.kafka.tool.KafkaTopicReport;
import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.kafka.KafkaCluster;
import com.neverwinterdp.util.FileUtil;

/**
 * @author Tuan
 */
public class AckKafkaWriterTestRunner {
  static String NAME = "test";

  private KafkaCluster cluster;
  private KafkaTopicConfig config;
  
  public AckKafkaWriterTestRunner(String[] topicToolArgs) {
    config = new KafkaTopicConfig(topicToolArgs);
  }

  public void setUp() throws Exception {
    FileUtil.removeIfExist("./build/kafka", false);
    cluster = new KafkaCluster("./build/kafka", 1, config.replication);
    cluster.setReplication(config.replication);
    cluster.setNumOfPartition(config.numberOfPartition);
    cluster.start();
    Thread.sleep(2000);
  }

  public void tearDown() throws Exception {
    cluster.shutdown();
    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
    for (Thread sel : threadSet) {
      System.err.println("Thread: " + sel.getName());
    }
  }

  public void run() throws Exception {
    config.zkConnect = cluster.getZKConnect();
    KafkaTopicCheckTool topicCheckTool = new KafkaTopicCheckTool(config);
    KafkaTopicConfig config = topicCheckTool.getKafkaConfig();
    topicCheckTool.runAsDeamon();
    //Make sure that messgages are sending before start the failure simulator
    while(!topicCheckTool.getKafkaMessageSendTool().isSending()) {
      Thread.sleep(100);
    }
    KafkapartitionLeaderKiller leaderKiller = new KafkapartitionLeaderKiller(config.topic, 0, 3000);
    new Thread(leaderKiller).start();
    topicCheckTool.waitForTermination();
    leaderKiller.exit();
    leaderKiller.waitForTermination(10000);
    KafkaTopicReport topicReport = topicCheckTool.getKafkaTopicReport();
    topicReport.setFailureSimulation(leaderKiller.failureCount);
    topicReport.report(System.out);
  }

  class KafkapartitionLeaderKiller implements Runnable {
    private String topic;
    private int partition;
    private long sleepBeforeRestart = 500;
    private int failureCount;
    private boolean exit = false;

    KafkapartitionLeaderKiller(String topic, int partition, long sleepBeforeRestart) {
      this.topic = topic;
      this.partition = partition;
      this.sleepBeforeRestart = sleepBeforeRestart;
    }

    public int getFaillureCount() {
      return this.failureCount;
    }

    public void exit() {
      exit = true;
    }

    public void run() {
      try {
        while (!exit) {
          KafkaTool kafkaTool = new KafkaTool(topic, cluster.getZKConnect());
          kafkaTool.connect();
          TopicMetadata topicMeta = kafkaTool.findTopicMetadata(topic);
          PartitionMetadata partitionMeta = findPartition(topicMeta, partition);
          Broker partitionLeader = partitionMeta.leader();
          Server kafkaServer = cluster.findKafkaServerByPort(partitionLeader.port());
          System.out.println("Shutdown kafka server " + kafkaServer.getPort());
          kafkaServer.shutdown();
          failureCount++;
          Thread.sleep(sleepBeforeRestart);
          kafkaServer.start();
          kafkaTool.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      synchronized (this) {
        notify();
      }
    }

    PartitionMetadata findPartition(TopicMetadata topicMetadata, int partion) {
      for (PartitionMetadata sel : topicMetadata.partitionsMetadata()) {
        if (sel.partitionId() == partition)
          return sel;
      }
      throw new RuntimeException("Cannot find the partition " + partition);
    }

    synchronized public void waitForTermination(long timeout) throws InterruptedException {
      wait(timeout);
    }
  }
}