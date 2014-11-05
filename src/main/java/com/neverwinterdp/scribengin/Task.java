package com.neverwinterdp.scribengin;


/**
 * The action to be performed on the data.
 * Allows spinning off several separate tasks.
 * 
 * Possible implementations
 * mapping, 
 * validation, 
 * partitioning 
 * filtering 
 * 
 * */
public interface Task {

  public void action();
}
