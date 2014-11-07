package com.neverwinterdp.scribengin;


/**
 * The following source implementations are available
 *  Kafka
 *  Kinesis
 *  JMS
 *  HDFS
 *  S3
 * 
 * Similar to a storm Spout
 * */
public interface Source /*extends Iterable<Source>*/{

  public SourceReader getReader() ;
}
