package com.neverwinterdp.scribengin;


/**
 * Where we write the data. The end system
 * The following Sinks are implemented.
 *  HDFS
 *  Kafka
 *  S3
 *  ElasticSearch
 * */
public interface Sink {

  public void write();
  
  class Partitioner{
    
  }
  
  class RecordWriter{
    
  }
}
