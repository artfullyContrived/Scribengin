package com.neverwinterdp.scribengin.dataflow.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neverwinterdp.registry.util.RegistryDebugger;
import com.neverwinterdp.scribengin.builder.ScribenginClusterBuilder;
import com.neverwinterdp.scribengin.client.shell.ScribenginShell;
import com.neverwinterdp.scribengin.tool.EmbededVMClusterBuilder;
import com.neverwinterdp.vm.tool.VMClusterBuilder;

public class DataflowWorkerNodeDebuggerUnitTest {
  static {
    System.setProperty("java.net.preferIPv4Stack", "true");
    System.setProperty("log4j.configuration", "file:src/test/resources/test-log4j.properties");
  }

  protected static ScribenginClusterBuilder clusterBuilder;
  protected static ScribenginShell          shell;

  @BeforeClass
  public static void setup() throws Exception {
    clusterBuilder = new ScribenginClusterBuilder(getVMClusterBuilder());
    clusterBuilder.clean();
    clusterBuilder.startVMMasters();
    Thread.sleep(3000);
    clusterBuilder.startScribenginMasters();
    shell = new ScribenginShell(clusterBuilder.getVMClusterBuilder().getVMClient());
  }

  @AfterClass
  public static void teardown() throws Exception {
    clusterBuilder.shutdown();
  }

  protected static VMClusterBuilder getVMClusterBuilder() throws Exception {
    return new EmbededVMClusterBuilder();
  }

  @Test
  public void testDataflowWorkerNodeDebugger() throws Exception {
    RegistryDebugger debugger = new RegistryDebugger(System.out, shell.getVMClient().getRegistry()) ;
    
    new Thread(){
      public void run() {
        try {
          Thread.sleep(5000);
          shell.execute("registry dump");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }.start();
    
    debugger.watchChild("/scribengin/dataflows/running/hello-kafka-dataflow/workers/active", 
                    ".*", new DataflowWorkerNodeDebugger());

    shell.execute("dataflow-test kafka --worker 3 --executor-per-worker 1 --duration 70000 --task-max-execute-time 1000 --source-name input --source-num-of-stream 10 --source-write-period 5 --source-max-records-per-stream 3000 --sink-name output");
    shell.execute("registry dump");
  }
}
