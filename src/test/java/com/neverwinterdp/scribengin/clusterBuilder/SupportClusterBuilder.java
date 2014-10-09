package com.neverwinterdp.scribengin.clusterBuilder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.neverwinterdp.scribengin.utilities.Util.isOpen;

import java.io.IOException;

import com.neverwinterdp.scribengin.fixture.Fixture;
import com.neverwinterdp.scribengin.fixture.KafkaFixture;
import com.neverwinterdp.scribengin.fixture.ZookeeperFixture;
import com.neverwinterdp.util.FileUtil;

/**
 * Brings up kafka, zookeeper, hadoop
 * @author Richard Duarte
 *
 */
public class SupportClusterBuilder {
  static {
    System.setProperty("app.dir", "build/cluster");
    System.setProperty("app.config.dir", "src/app/config");
    System.setProperty("log4j.configuration", "file:src/app/config/log4j.properties");
  }

  private static String MINI_CLUSTER_PATH = "/tmp/miniCluster";
  UnitTestCluster hadoopServer;
  Fixture zkFixture, kafkaFixture;
  String hadoopConnection = "";


  public SupportClusterBuilder(String version, String zkHost, int zkPort, String kafkaHost,
      int kafkaPort) throws Exception {
    checkArgument(!isOpen(kafkaPort), "The requested kakfka Port:" + kafkaPort
        + " is already in use.");
    checkArgument(!isOpen(zkPort), "The requested zookeeper Port:" + zkPort + " is already in use.");
    

    FileUtil.removeIfExist("build/cluster", false);
    zkFixture = new ZookeeperFixture(version, zkHost, zkPort);
    kafkaFixture = new KafkaFixture(version, kafkaHost, kafkaPort, zkHost, zkPort);
    hadoopServer = UnitTestCluster.instance(MINI_CLUSTER_PATH);
  }


  public String getHadoopConnection() {
    return this.hadoopConnection;
  }

  
  public void install() throws InterruptedException, IOException {
    hadoopServer.build(3);
    hadoopConnection = hadoopServer.getUrl();
    kafkaFixture.install();
    System.out.println("Now we go to start kafka");
    zkFixture.start();
    kafkaFixture.start();
    Thread.sleep(5000);
  }

  public void uninstall() throws IOException {
    hadoopServer.destroy();
    kafkaFixture.stop();
    zkFixture.stop();
    //TODO uninstall kafka
    //TODO uninstall zookeeper
  }
}