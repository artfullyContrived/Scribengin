package com.neverwinterdp.vm.client;

import static com.neverwinterdp.vm.tool.VMClusterBuilder.h1;

import java.io.IOException;
import java.util.List;
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
    return execute(vmDescriptor, command, 5000);
  }
  
  public CommandResult<?> execute(VMDescriptor vmDescriptor, Command command, long timeout) throws Exception {
    CommandPayload payload = new CommandPayload(command, null) ;
    Node node = registry.create(vmDescriptor.getRegistryPath() + "/commands/command-", payload, NodeCreateMode.EPHEMERAL_SEQUENTIAL);
    CommandReponseWatcher responseWatcher = new CommandReponseWatcher(registry, node.getPath());
    node.watchModify(responseWatcher);
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
    if(result.isDiscardResult()) return true;
    return result.getResultAs(Boolean.class);
  }
  
  public boolean simulateKill(VMDescriptor vmDescriptor) throws Exception {
    CommandResult<?> result = execute(vmDescriptor, new VMCommand.SimulateKill());
    if(result.isDiscardResult()) return true;
    return result.getResultAs(Boolean.class);
  }
  
  public boolean kill(VMDescriptor vmDescriptor) throws Exception {
    CommandResult<?> result = execute(vmDescriptor, new VMCommand.Kill());
    if(result.isDiscardResult()) return true;
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
  
  static public class CommandReponseWatcher extends NodeWatcher {
    private Registry         registry;
    private String           path;
    private CommandResult<?> result;
    private Exception        error;
    private boolean          discardResult = false;
    
    public CommandReponseWatcher(Registry registry, String path) {
      this.registry = registry;
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
      } catch(Exception e) {
        error = e ;
      } finally {
        notifyForResult() ;
      }
    }
    synchronized void notifyForResult() {
      notifyAll() ;
    }
    
    synchronized public CommandResult<?> waitForResult(long timeout) throws Exception {
      if(result == null) {
        wait(timeout);
      }
      if(discardResult) {
        result = new CommandResult<Object>() ;
        result.setDiscardResult(true);
      }
      if(error != null) throw error;
      if(result == null) {
        throw new TimeoutException("Cannot get the result after " + timeout + "ms") ;
      }
      return result ;
    }
  }
}