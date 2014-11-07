package com.neverwinterdp.scribengin;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;

/**
 * A daemon running on the worker Nodes.
 * 
 * Every worker node runs a Supervisor daemon
 * 
 * A supervisor communicates with the {@link ScribeMaster} via zookeeper to determine which data flow should be running on that Machine.
 * The supervisor listens for work assigned to it and starts and stops StreamCoordinators as necessary based on what the ScribeMaster has assigned to it.
 * 
 * @see Zookeeper
 * 
 * */
public class Superviser implements Service, Runnable{

  @Override
  public void run() {
    // TODO Auto-generated method stub
    
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

}
