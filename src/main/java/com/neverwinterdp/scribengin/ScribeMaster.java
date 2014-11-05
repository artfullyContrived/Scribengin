package com.neverwinterdp.scribengin;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;

/**
 * Main coordinator of all jobs. Similar to hadoop's job tracker.
 * 1. xxxxx Submits jobs to Scribemaster.
 * 2. Locate nodes that can be given jobs
 * 3. ScribeMaster then submits tasks to tasktracker? nodes
 * 4. Also monitors tasktracker? heartbeat, failure 
 * 5. Sends info to xxxxxx about progress
 * 
 * */
public class ScribeMaster implements Service {
  //Start and monitor many Scribe coordinators

  private Set<DataFlow> dataFlows;

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
