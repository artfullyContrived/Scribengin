package com.neverwinterdp.scribengin;

import java.util.Map;

import com.google.common.util.concurrent.Service;


/**
 * A data-flow is a named job that describes 
 * 
 * 1. what data you want to read (Source), 
 * 2. what you want to do to the data(Task) and 
 * 3. where you want that data written to(Sink). 
 * 
 * The set of data you want to read is split into logical partitions (Streams) to be processed by workers (Scribes).
 * A data-flow is made up of multiple streams of data that are being processed. 
 * Each stream is processed by a single Scribe worker.
 * 
 * */
public abstract class DataFlow implements Service{
  
  private String name;
  
  private Map<Stream, Scribe> scribes;
  
  public DataFlow create() {
    return null;
    
  }
  
  
  public Status getStatus() {
    return null;
    
  }
  
  public Status updateStatus() {
    return null;
    
  }
  
  public Status kill() {
    return null;
    
  }
  

}
