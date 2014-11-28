/**
 * 
 */
package com.neverwinterdp.scribengin.source.kafka;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Anthony
 *
 */
//TODO that the topic node listener reacts to topic delete
public class TopicNodeListenerTest {

  static {
    System.setProperty("log4j.configuration", "file:src/test/resources/log4j.properties");
  }
  private static TopicNodeListenerStub stub;
  private final static String topic = "scribe888";
  private static int partitions = 2;
  private static int kafkaPort = 9092;
  private static String zkHost = "127.0.0.1";
  private static String kafkaHost = "127.0.0.1";
  private static String version = "0.8.1";
  private static int zkPort = 2181;
  private static Servers servers;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    stub = new TopicNodeListenerStub(topic);
    servers = new Servers(partitions, kafkaPort, zkPort, zkHost, version, kafkaHost, topic);
    servers.start();
  }


  /**
   * Test method for {@link com.neverwinterdp.scribengin.source.kafka.TopicNodeListener#update()}.
   */
  @Test
  public void testUpdate() {
    //set topic node listener
    try {
      servers.getZkUtils().setTopicNodeListener(stub);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      //update topic
      servers.getZkUtils().addPartitions(topic, partitions + 2);
    } catch (Exception e) {
      e.printStackTrace();
    }
    //count update from stub 
    assertEquals(2, stub.getUpdates());
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    servers.stop();
  }


}


class TopicNodeListenerStub extends TopicNodeListener {

  private static Integer updates;
  private String topic;



  public TopicNodeListenerStub(String topic) {
    logger.info("HOHOHO");
    this.topic = topic;
    updates = 0;
  }

  @Override
  public void update() {
    logger.info("Updated " + (updates = updates + 1));
  }

  @Override
  public String getTopic() {
    return topic;
  }

  public int getUpdates() {
    logger.info("Updated2 " + updates);
    return updates;
  }
}
