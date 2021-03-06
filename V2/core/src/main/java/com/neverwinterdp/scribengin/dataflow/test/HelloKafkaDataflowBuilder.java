package com.neverwinterdp.scribengin.dataflow.test;

import java.util.Random;

import com.neverwinterdp.kafka.tool.KafkaTool;
import com.neverwinterdp.scribengin.Record;
import com.neverwinterdp.scribengin.ScribenginClient;
import com.neverwinterdp.scribengin.dataflow.DataflowDescriptor;
import com.neverwinterdp.scribengin.dataflow.DataflowTaskContext;
import com.neverwinterdp.scribengin.event.ScribenginWaitingEventListener;
import com.neverwinterdp.scribengin.scribe.ScribeAbstract;
import com.neverwinterdp.scribengin.storage.StorageDescriptor;

public class HelloKafkaDataflowBuilder {
  private String     name  = "hello";
  private String     zkConnect              = "127.0.0.1:2181";
  private String     topic                  = "hello";

  private int numOfWorkers = 3;
  private int numOfExecutorPerWorker = 3;
  private ScribenginClient scribenginClient;
  
  public HelloKafkaDataflowBuilder(ScribenginClient scribenginClient) {
    this.scribenginClient = scribenginClient;
  }

  
  public void setNumOfWorkers(int numOfWorkers) {
    this.numOfWorkers = numOfWorkers;
  }

  public void setNumOfExecutorPerWorker(int numOfExecutorPerWorker) {
    this.numOfExecutorPerWorker = numOfExecutorPerWorker;
  }

  public ScribenginWaitingEventListener submit() throws Exception {
    KafkaTool client = new KafkaTool(name, zkConnect) ;
    client.connect();
    String brokerList = client.getKafkaBrokerList() ;
    client.close();

    DataflowDescriptor dflDescriptor = new DataflowDescriptor();
    dflDescriptor.setName("hello-kafka-dataflow");
    dflDescriptor.setNumberOfWorkers(numOfWorkers);
    dflDescriptor.setNumberOfExecutorsPerWorker(numOfExecutorPerWorker);
    dflDescriptor.setScribe(TestCopyScribe.class.getName());
    
    StorageDescriptor storageDescriptor = new StorageDescriptor("KAFKA") ;
    storageDescriptor.attribute("name", name);
    storageDescriptor.attribute("topic", topic + ".source");
    storageDescriptor.attribute("zk.connect", zkConnect);
    storageDescriptor.attribute("broker.list", brokerList);
    dflDescriptor.setSourceDescriptor(storageDescriptor);
    
    StorageDescriptor defaultSink = new StorageDescriptor("KAFKA");
    defaultSink.attribute("name", name);
    defaultSink.attribute("topic", topic + ".sink.default");
    defaultSink.attribute("zk.connect", zkConnect);
    defaultSink.attribute("broker.list", brokerList);
    dflDescriptor.addSinkDescriptor("default", defaultSink);
    
    StorageDescriptor invalidSink = new StorageDescriptor("KAFKA");
    invalidSink.attribute("name", name);
    invalidSink.attribute("topic", topic + ".sink.invalid");
    invalidSink.attribute("zk.connect", zkConnect);
    invalidSink.attribute("broker.list", brokerList);
    dflDescriptor.addSinkDescriptor("invalid", invalidSink);
    return scribenginClient.submit(dflDescriptor);
  }
  
  
  static public class TestCopyScribe extends ScribeAbstract {
    private int count = 0;
    private Random random = new Random();
    
    @Override
    public void process(Record record, DataflowTaskContext ctx) throws Exception {
      if(random.nextDouble() < 0.8) {
        ctx.append(record);
        //System.out.println("Write default");
      } else {
        ctx.write("invalid", record);
        //System.out.println("Write invalid");
      }
      count++ ;
      if(count == 100) {
        ctx.commit();
        count = 0;
      }
    }
  }
}
