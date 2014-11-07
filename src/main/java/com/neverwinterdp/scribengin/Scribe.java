package com.neverwinterdp.scribengin;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;

/**
 * Processes a partition of data(Stream) from a data-flow
 * The actual worker.  Holds a Reader, Task and Writer, (Source, Task, Sink)
 * Is started by a StreamCoordinator.
 * Each individual Scribe only processes data from a single partition(Stream) from a single data-flow.
 * 
 * A scribe is started by a StreamCoordinator.
 * 
 * xxxxxx of a Storm Bolt.
 * 
 * */
public class Scribe implements Runnable, Service {


  private Source source;

  private Task task;

  private Sink sink;



  public Scribe(Source source, Task task, Sink sink) {
    super();
    this.source = source;
    this.task = task;
    this.sink = sink;
  }

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

  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  public Sink getSink() {
    return sink;
  }

  public void setSink(Sink sink) {
    this.sink = sink;
  }
}
