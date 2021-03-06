package com.neverwinterdp.scribengin.dataflow;

import java.util.List;

import com.neverwinterdp.registry.RegistryException;
import com.neverwinterdp.scribengin.ScribenginClient;
import com.neverwinterdp.vm.VMDescriptor;

public class DataflowClient {
  private ScribenginClient scribenginClient ;
  private DataflowRegistry dflRegistry ;
  
  public DataflowClient(ScribenginClient scribenginClient, String dataflowPath) throws Exception {
    this.scribenginClient = scribenginClient;
    this.dflRegistry = new DataflowRegistry(scribenginClient.getRegistry(), dataflowPath) ;
  }
  
  public ScribenginClient getScribenginClient() { return this.scribenginClient; }
  
  
  public VMDescriptor getDataflowMaster() throws RegistryException { 
    return dflRegistry.getDataflowMaster() ;
  }
  
  public List<VMDescriptor> getDataflowMasters() throws RegistryException {
    return dflRegistry.getDataflowMasters();
  }
  
  public List<VMDescriptor> getDataflowWorkers() throws RegistryException {
    return dflRegistry.getActiveWorkers();
  }
  
  public void setDataflowTaskEvent(DataflowTaskEvent event) throws RegistryException {
    dflRegistry.setDataflowTaskMasterEvent(event);
  }
}