package com.neverwinterdp.vm.jvm;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.beust.jcommander.JCommander;
import com.neverwinterdp.registry.zk.RegistryImpl;
import com.neverwinterdp.vm.VM;
import com.neverwinterdp.vm.VMConfig;
import com.neverwinterdp.vm.VMDescriptor;
import com.neverwinterdp.vm.VMDummyApp;
import com.neverwinterdp.vm.VMStatus;
import com.neverwinterdp.vm.client.VMClient;
import com.neverwinterdp.vm.client.shell.Shell;
import com.neverwinterdp.vm.command.CommandResult;
import com.neverwinterdp.vm.command.VMCommand;
import com.neverwinterdp.vm.environment.jvm.JVMVMServicePlugin;
import com.neverwinterdp.vm.junit.VMAssert;
import com.neverwinterdp.vm.junit.VMCluster;
import com.neverwinterdp.vm.service.VMServiceApp;
import com.neverwinterdp.vm.service.VMServiceCommand;
import com.neverwinterdp.vm.service.VMServicePlugin;

public class VMManagerAppUnitTest  {
  VMCluster  vmCluster ;
  Shell      shell;
  VMClient   vmClient;
  
  @Before
  public void setup() throws Exception {
    vmCluster = new VMCluster() ;
    vmCluster.clean();
    vmCluster.start();
    Thread.sleep(2000);
    shell = vmCluster.newShell() ;
    vmClient = shell.getVMClient();
  }
  
  @After
  public void teardown() throws Exception {
    vmCluster.shutdown();
  }
  
  @Test
  public void testMaster() throws Exception {
    VMAssert vmAssert = new VMAssert(shell.getVMClient().getRegistry());
    vmAssert.assertVMStatus("Expect vm-master-1 with running status", "vm-master-1", VMStatus.RUNNING);
    vmAssert.assertHeartbeat("Expect vm-master-1 has connected heartbeat", "vm-master-1", true);
    vmAssert.assertVMStatus("Expect vm-master-2 with running status", "vm-master-2", VMStatus.RUNNING);
    vmAssert.assertHeartbeat("Expect vm-master-2 has connected heartbeat", "vm-master-2", true);
    
    banner("Create Masters");
    VM vmMaster1 = createVMMaster("vm-master-1");
    Thread.sleep(1000);
    VM vmMaster2 = createVMMaster("vm-master-2");
    try {
      vmAssert.waitForEvents(5000);
    } catch(Exception ex) {
      vmAssert.reset();
      ex.printStackTrace();
    }
    shell.execute("registry dump");

    banner("Create VM Dummy 1");
    vmAssert.assertVMStatus("Expect vm-dummy-1 with running status", "vm-dummy-1", VMStatus.RUNNING);
    vmAssert.assertHeartbeat("Expect vm-dummy-1 has connected heartbeat", "vm-dummy-1", true);
    VMDescriptor vmDummy1 = allocate(vmClient, "vm-dummy-1") ;
    try {
      vmAssert.waitForEvents(5000);
    } catch(Exception ex) {
      vmAssert.reset();
      ex.printStackTrace();
    }
    shell.execute("registry dump");
    
    banner("Shutdown VM Master 1");
    //shutdown vm master 1 , the vm-master-2 should pickup the leader role.
    vmAssert.assertVMStatus("Expect vm-master-1 with running status", "vm-master-1", VMStatus.TERMINATED);
    vmAssert.assertHeartbeat("Expect vm-master-1 has connected heartbeat", "vm-master-1", false);
    vmAssert.assertVMMaster("Expect the vm-master-2 will be elected", "vm-master-2");
    vmMaster1.shutdown();
    vmAssert.waitForEvents(5000);
    shell.execute("registry dump");
    
    banner("Create VM Dummy 2");
    VMDescriptor vmDummy2 = allocate(vmClient, "vm-dummy-2") ;
    shell.execute("registry dump");
    
    banner("Shutdown VM Dummy 1 and 2");
    vmAssert.assertVMStatus("Expect vm-dummy-1 running status", "vm-dummy-1", VMStatus.TERMINATED);
    vmAssert.assertVMStatus("Expect vm-dummy-2 running status", "vm-dummy-2", VMStatus.TERMINATED);
    Assert.assertTrue(shutdown(vmClient, vmDummy2));
    Assert.assertTrue(shutdown(vmClient, vmDummy1));
    vmAssert.waitForEvents(5000);
    shell.execute("registry dump");
  }

  private void banner(String title) {
    System.out.println("\n\n");
    System.out.println("------------------------------------------------------------------------");
    System.out.println(title);
    System.out.println("------------------------------------------------------------------------");
  }
  
  private VM createVMMaster(String name) throws Exception {
    String[] args = {
      "--name", name,
      "--roles", "vm-master",
      "--self-registration",
      "--registry-connect", "127.0.0.1:2181", 
      "--registry-db-domain", "/NeverwinterDP", 
      "--registry-implementation", RegistryImpl.class.getName(),
      "--vm-application",VMServiceApp.class.getName(),
      "--prop:implementation:" + VMServicePlugin.class.getName() + "=" + JVMVMServicePlugin.class.getName()
    };
    VM vm = VM.run(args);
    return vm;
  }
  
  private VMDescriptor allocate(VMClient vmClient, String name) throws Exception {
    VMDescriptor masterVMDescriptor = vmClient.getMasterVMDescriptor();
    String[] args = {
      "--name", name,
      "--roles", "dummy",
      "--registry-connect", "127.0.0.1:2181", 
      "--registry-db-domain", "/NeverwinterDP", 
      "--registry-implementation", RegistryImpl.class.getName(),
      "--vm-application", VMDummyApp.class.getName()
    };
    VMConfig vmConfig = new VMConfig() ;
    new JCommander(vmConfig, args);
    CommandResult<?> result = vmClient.execute(masterVMDescriptor, new VMServiceCommand.Allocate(vmConfig));
    Assert.assertNull(result.getErrorStacktrace());
    VMDescriptor vmDescriptor = result.getResultAs(VMDescriptor.class);
    Assert.assertNotNull(vmDescriptor);
    return vmDescriptor;
  }
  
  private boolean shutdown(VMClient vmClient, VMDescriptor vmDescriptor) throws Exception {
    CommandResult<?> result = vmClient.execute(vmDescriptor, new VMCommand.Shutdown());
    Assert.assertNull(result.getErrorStacktrace());
    return result.getResultAs(Boolean.class);
  }
}