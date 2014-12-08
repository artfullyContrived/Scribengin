package com.neverwinterdp.scribengin.dataflow;

import java.util.Map;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.neverwinterdp.module.AppModule;
import com.neverwinterdp.registry.Registry;
import com.neverwinterdp.registry.RegistryConfig;
import com.neverwinterdp.registry.election.LeaderElection;
import com.neverwinterdp.registry.election.LeaderElectionListener;
import com.neverwinterdp.vm.VMApp;
import com.neverwinterdp.vm.VMService;


public class VMDataflowMasterApp extends VMApp {
  private LeaderElection election ;
  private Injector  appContainer ;
  
  @Override
  public void run() throws Exception {
    election = new LeaderElection(getVM().getVMRegistry().getRegistry(), VMService.LEADER_PATH) ;
    election.setListener(new MasterLeaderElectionListener());
    election.start();
    try {
      waitForShutdown();
    } catch(InterruptedException ex) {
    } finally {
      if(election != null && election.getLeaderId() != null) {
        election.stop();
      }
    }
  }
 
  class MasterLeaderElectionListener implements LeaderElectionListener {
    @Override
    public void onElected() {
      try {
        final Registry registry = getVM().getVMRegistry().getRegistry();
        registry.setData(VMService.LEADER_PATH, getVM().getDescriptor());
        AppModule module = new AppModule(getVM().getDescriptor().getVmConfig().getProperties()) {
          @Override
          protected void configure(Map<String, String> properties) {
            bindInstance(RegistryConfig.class, registry.getRegistryConfig());
            try {
              bindType(Registry.class, registry.getClass().getName());
            } catch (ClassNotFoundException e) {
              //TODO: use logger
              e.printStackTrace();
            }
          };
        };
        appContainer = Guice.createInjector(module);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
}