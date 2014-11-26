package com.neverwinterdp.scribengin.fixture;

import java.util.Random;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

import org.apache.log4j.Logger;

public class SimplePartitioner implements Partitioner {

  private static final Logger logger = Logger.getLogger(SimplePartitioner.class);

  public SimplePartitioner(VerifiableProperties props) {

  }

  public int partition(Object key, int numPartitions) {
    int partition = new Random().nextInt(numPartitions);
    logger.info("TotalPartitions: " + numPartitions + " We write to: " + partition);

    return partition;
  }
}
