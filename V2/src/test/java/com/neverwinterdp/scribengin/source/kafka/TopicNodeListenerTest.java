/**
 * 
 */
package com.neverwinterdp.scribengin.source.kafka;

import static org.junit.Assert.*;
import kafka.utils.ZkUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neverwinterdp.scribengin.fixture.ZookeeperFixture;
import com.neverwinterdp.scribengin.util.ZookeeperUtils;

/**
 * @author Anthony
 *
 */
public class TopicNodeListenerTest {

  private static TopicNodeListener ha;
  private static ZookeeperUtils zkUtils;
  private static String zookeeperURL;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  
    
    ha = new TopicNodeListenerStub();
   
    
    
    zkUtils = new ZookeeperUtils(zookeeperURL);

  }


  /**
   * Test method for {@link com.neverwinterdp.scribengin.source.kafka.TopicNodeListener#update()}.
   */
  @Test
  public void testUpdate() {
    fail("Not yet implemented");
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}


}


class TopicNodeListenerStub extends TopicNodeListener {

  int updates = 0;

  @Override
  public void update() {
    updates += 1;

  }

  @Override
  public String getTopic() {
    // TODO Auto-generated method stub
    return null;
  }

}
