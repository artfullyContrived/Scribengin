package com.neverwinterdp.scribengin;

/**
 * Coordinator of a data flow.
 * One coordinator is created for each data-flow. 
 * The coordinator helps orchestrate which worker(Scribe) 
 * should get what partition(Stream) of the data to process
 * */
public class StreamCoordinator {

  String dataFlowName;
}
