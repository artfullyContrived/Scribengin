package com.neverwinterdp.scribengin;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;

/**
 * Coordinator of a data flow.
 * One coordinator is created for each data flow. 
 * The coordinator helps orchestrate which worker(Scribe) 
 * should get what partition(Stream) of the data to process
 * */
public class StreamCoordinator implements Service {

  private String dataFlowName;

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


  @Override
  public ListenableFuture<State> start() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public State startAndWait() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Service startAsync() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public boolean isRunning() {
    // TODO Auto-generated method stub
    return false;
  }


  @Override
  public State state() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public ListenableFuture<State> stop() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public State stopAndWait() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Service stopAsync() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public void awaitRunning() {
    // TODO Auto-generated method stub
    
  }


  @Override
  public void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException {
    // TODO Auto-generated method stub
    
  }


  @Override
  public void awaitTerminated() {
    // TODO Auto-generated method stub
    
  }


  @Override
  public void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException {
    // TODO Auto-generated method stub
    
  }


  @Override
  public Throwable failureCause() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public void addListener(Listener listener, Executor executor) {
    // TODO Auto-generated method stub
    
  }


  public String getDataFlowName() {
    return dataFlowName;
  }


  public void setDataFlowName(String dataFlowName) {
    this.dataFlowName = dataFlowName;
  }


  public Map<Stream, Scribe> getScribes() {
    return scribes;
  }


  public void setScribes(Map<Stream, Scribe> scribes) {
    this.scribes = scribes;
  }

}
