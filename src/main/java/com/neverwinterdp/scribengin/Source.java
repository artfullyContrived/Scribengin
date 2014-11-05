package com.neverwinterdp.scribengin;


/**
 * A reader of a source. the following source implementations are available
 *  Kafka
 *  Kinesis
 *  JMS
 *  HDFS
 *  S3
 * 
 * */
public interface Source /*extends Iterable<Source>*/{


  public void read();
}
