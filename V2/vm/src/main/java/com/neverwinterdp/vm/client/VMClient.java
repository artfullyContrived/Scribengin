package com.neverwinterdp.vm.client;

import static com.neverwinterdp.vm.tool.VMClusterBuilder.h1;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import com.mycila.jmx.annotation.JmxBean;
import com.neverwinterdp.registry.Node;
import com.neverwinterdp.registry.NodeCreateMode;
import com.neverwinterdp.registry.Registry;
import com.neverwinterdp.registry.RegistryException;
import com.neverwinterdp.registry.event.NodeEvent;
import com.neverwinterdp.registry.event.NodeWatcher;
import com.neverwinterdp.vm.VMConfig;
import com.neverwinterdp.vm.VMDescriptor;
import com.neverwinterdp.vm.command.Command;
import com.neverwinterdp.vm.command.CommandPayload;
import com.neverwinterdp.vm.command.CommandResult;
import com.neverwinterdp.vm.command.VMCommand;
import com.neverwinterdp.vm.service.VMService;
import com.neverwinterdp.vm.service.VMServiceCommand;

@JmxBean("role=vm-client, type=VMClient, name=VMClient")
public class VMClient {
  private Registry registry;

  public VMClient(Registry registry) {
    this.registry = registry;
  }
  
  public Registry getRegistry() { return this.registry ; }
  
  public List<VMDescriptor> getRunningVMDescriptors() throws RegistryException {
    return registry.getChildrenAs(VMService.ALLOCATED_PATH, VMDescriptor.class) ;
  }
  
  public List<VMDescriptor> getHistoryVMDescriptors() throws RegistryException {
    return registry.getChildrenAs(VMService.HISTORY_PATH, VMDescriptor.class) ;
  }
  
  public VMDescriptor getMasterVMDescriptor() throws RegistryException { 
    Node vmNode = registry.getRef(VMService.LEADER_PATH);
    return vmNode.getDataAs(VMDescriptor.class);
  }
  
  public void shutdown() throws Exception {
    h1("Shutdow the vm masters");
    registry.create(VMService.SHUTDOWN_EVENT_PATH, true, NodeCreateMode.PERSISTENT);
  }
  
  public CommandResult<?> execute(VMDescriptor vmDescriptor, Command command) throws RegistryException, Exception {
    return execute(vmDescriptor, command, 60000);
  }
  
  public CommandResult<?> execute(VMDescriptor vmDescriptor, Command command, long timeout) throws Exception {
    CommandPayload payload = new CommandPayload(command, null) ;
    Node node = registry.create(vmDescriptor.getRegistryPath() + "/commands/command-", payload, NodeCreateMode.EPHEMERAL_SEQUENTIAL);
    CommandReponseWatcher responseWatcher = new CommandReponseWatcher(node.getPath());
    node.watch(responseWatcher);
    return responseWatcher.waitForResult(timeout);
  }
  
  public void execute(VMDescriptor vmDescriptor, Command command, CommandCallback callback) {
  }
  
  public VMDescriptor allocate(VMConfig vmConfig) throws Exception {
    VMDescriptor masterVMDescriptor = getMasterVMDescriptor();
    CommandResult<VMDescriptor> result = 
        (CommandResult<VMDescriptor>) execute(masterVMDescriptor, new VMServiceCommand.Allocate(vmConfig));
    if(result.getErrorStacktrace() != null) {
      registry.get("/").dump(System.err);
      throw new Exception(result.getErrorStacktrace());
    }
    return result.getResult();
  }
  
  public VMDescriptor allocate(String localAppHome, VMConfig vmConfig) throws Exception {
    return allocate(vmConfig);
  }
  
  public boolean shutdown(VMDescriptor vmDescriptor) throws Exception {
    CommandResult<?> result = execute(vmDescriptor, new VMCommand.Shutdown());
    return result.getResultAs(Boolean.class);
  }
  
  public boolean simulateKill(VMDescriptor vmDescriptor) throws Exception {
    CommandResult<?> result = execute(vmDescriptor, new VMCommand.SimulateKill());
    return result.getResultAs(Boolean.class);
  }
  
  public boolean kill(VMDescriptor vmDescriptor) throws Exception {
    CommandResult<?> result = execute(vmDescriptor, new VMCommand.Kill());
    return result.getResultAs(Boolean.class);
  }
  
  public FileSystem getFileSystem() throws IOException {
    return FileSystem.get(new Configuration());
  }
  
  public void uploadApp(String localAppHome, String appHome) throws Exception {
  }
  
  public void createVMMaster(String name) throws Exception {
    throw new RuntimeException("This method need to override") ;
  }
  
  public void configureEnvironment(VMConfig vmConfig) {
    throw new RuntimeException("This method need to override") ;
  }
  
  public class CommandReponseWatcher extends NodeWatcher {
    private String path ;
    private CommandResult<?> result ;
    private Exception error ;
    private boolean discardResult = false;
    
    public CommandReponseWatcher(String path) {
      this.path = path;
    }
    
    @Override
    public void onEvent(NodeEvent event) {
      String path = event.getPath();
      try {
        if(event.getType() == NodeEvent.Type.DELETE) {
          discardResult = true ;
          return ;
        } else {
          CommandPayload payload = registry.getDataAs(path, CommandPayload.class) ;
          result = payload.getResult() ;
          registry.delete(path);
        }
        synchronized(this) {
          notify();
        }
      } catch(Exception e) {
        error = e ;
      }
    }
    
    public CommandResult<?> waitForResult(long timeout) throws Exception {
      if(result == null) {
        synchronized(this) {
          wait(timeout);
        }
      }
      if(error != null) throw error;
      if(discardResult) {
        result = new CommandResult<Object>() ;
        result.setDiscardResult(true);
      }
      //Check one more time in case the server return the response before the watcher is registered
      if(result == null) {
        CommandPayload payload = registry.getDataAs(path, CommandPayload.class) ;
        result = payload.getResult() ;
      }
      if(result == null) {
        throw new TimeoutException("Cannot get the result after " + timeout + "ms") ;
      }
      return result ;
    }
  }
}