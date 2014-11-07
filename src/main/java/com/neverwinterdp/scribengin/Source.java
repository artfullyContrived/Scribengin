package com.neverwinterdp.scribengin;


/**
 * The following source implementations are available
 *  Kafka
 *  Kinesis
 *  JMS
 *  HDFS
 *  S3
 * 
 * */
public interface Source /*extends Iterable<Source>*/{

  public SourceReader getReader() ;
}
