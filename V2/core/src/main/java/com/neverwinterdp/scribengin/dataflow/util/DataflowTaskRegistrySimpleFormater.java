package com.neverwinterdp.scribengin.dataflow.util;

import com.neverwinterdp.registry.Node;
import com.neverwinterdp.registry.util.NodeFormatter;
import com.neverwinterdp.scribengin.dataflow.DataflowTaskDescriptor;
import com.neverwinterdp.scribengin.dataflow.DataflowTaskReport;
import com.neverwinterdp.vm.VMDescriptor;

/**
 * @author Tuan
 */
public class DataflowTaskRegistrySimpleFormater extends NodeFormatter {
  private Node taskDescriptorNode ;
  
  public DataflowTaskRegistrySimpleFormater(Node taskNode) {
    this.taskDescriptorNode = taskNode;
  }
  
  @Override
  public String getFormattedText() {
    StringBuilder b = new StringBuilder() ;
    try {
      if(!taskDescriptorNode.exists()){
        return "Dataflow task activityNode is already deleted or moved to the history";
      }
      
      DataflowTaskDescriptor dflDescriptor = taskDescriptorNode.getDataAs(DataflowTaskDescriptor.class);
      DataflowTaskReport     dflTaskReport = taskDescriptorNode.getChild("report").getDataAs(DataflowTaskReport.class);
      
      VMDescriptor workerDescriptor = null ;
      Node workerHeartbeatNode = 
          taskDescriptorNode.getParentNode().
          getParentNode().getDescendant("executors/assigned/" + taskDescriptorNode.getName() + "/heartbeat");
      
      
      b.append("  DataflowTaskDescriptor: ID = "+dflDescriptor.getId()+", Status: "+dflDescriptor.getStatus()+"\n");
      b.append("  DataflowTaskReport: ProcessCount = "+dflTaskReport.getProcessCount()+", "+
          "CommitProcessCount = "+dflTaskReport.getCommitProcessCount()+"\n");
      
      if(workerHeartbeatNode.exists()) {
        workerDescriptor = workerHeartbeatNode.getDataAs(VMDescriptor.class) ;
        b.append("  WorkerDescriptor: ID = "+workerDescriptor.getId()+", Path: "+workerDescriptor.getRegistryPath()+"\n");
      }
      
    } catch (Exception e) {
      e.printStackTrace();
      b.append(e.getMessage());
    }
    
    return b.toString();
  }

}