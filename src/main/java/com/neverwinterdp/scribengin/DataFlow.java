package com.neverwinterdp.scribengin;



/**
 * A DataFlow is a collection of multiple streams of data that are being processed. Each stream is processed by a single Scribe worker.
 * <p>
 * A data-flow is a named job that describes 
 * <ol>
 * <li> What data you want to read (Source), </li> 
 * <li> What you want to do to the data(Task) and </li> 
 * <li> Where you want that data written to(Sink). </li> 
 * </ol>
 * </p>
 * The set of data to be read is split into logical partitions (Streams) to be processed by workers (Scribes).
 * 
 * xxxxx of a Storm Topology
 */
public abstract class DataFlow {
 
  
  public static void main(String[] args) {

  }
}
