package com.neverwinterdp.scribengin.stream.sink;

import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

import com.neverwinterdp.scribengin.stream.sink.partitioner.SinkPartitioner;
import com.neverwinterdp.scribengin.tuple.Tuple;

public class RandomFailureSinkStream implements SinkStream{
  private int failurePercentage;
  private InMemorySinkStream mSink;
  private String name;
  
  public RandomFailureSinkStream(){
    this(50);
  }
  
  public RandomFailureSinkStream(int failurePercentage){
    if(failurePercentage < 100 && failurePercentage > -1){
      this.failurePercentage = failurePercentage;
    }
    else{
      this.failurePercentage = 50;
    }
    this.mSink = new InMemorySinkStream();
    this.name = this.getClass().getSimpleName() +"-"+UUID.randomUUID().toString();
  }
  
  @Override
  public boolean prepareCommit() {
    if(this.decideToFail()){
      return false;
    }
    return this.mSink.prepareCommit();
  }

  @Override
  public boolean commit() {
    if(this.decideToFail()){
      return false;
    }
    return this.mSink.commit();
  }

  @Override
  public boolean clearBuffer() {
    if(this.decideToFail()){
      return false;
    }
    return this.mSink.clearBuffer();
  }

  @Override
  public boolean completeCommit() {
    if(this.decideToFail()){
      return false;
    }
    
    return this.mSink.completeCommit();
  }

  @Override
  public boolean bufferTuple(Tuple t) {
    if(this.decideToFail()){
      return false;
    }
    return this.mSink.bufferTuple(t);
  }

  @Override
  public boolean rollBack() {
    if(this.decideToFail()){
      return false;
    }
    return this.mSink.rollBack();
  }

  @Override
  public void setSinkPartitioner(SinkPartitioner sp) {
    this.mSink.setSinkPartitioner(sp);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public long getBufferSize() {
    return this.mSink.getBufferSize();
  }
  
  public LinkedList<Tuple> getData(){
    return this.mSink.getData();
  }
  
  private boolean decideToFail(){
    Random rand = new Random();
    if(this.failurePercentage > rand.nextInt(101)){
      return true;
    }
    return false;
  }

}
