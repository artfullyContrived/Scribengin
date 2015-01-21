package com.neverwinterdp.vm.client.shell;

import java.util.HashMap;
import java.util.Map;

import com.neverwinterdp.registry.Registry;
import com.neverwinterdp.vm.client.VMClient;

public class Shell {
  protected Console console ;
  protected VMClient vmClient ;
  protected Map<String, Command> commands = new HashMap<String, Command>() ;
  
  public Shell(Registry registry) {
    this(registry, new Console());
  }
  
  public Shell(Registry registry, Console console){
    this.console = console ;
    vmClient = new VMClient(registry);
    add("registry", new RegistryCommand());
    add("vm", new VMCommand());
  }
  
  public Console console() { return this.console ; }
  
  public VMClient getVMClient() { return this.vmClient ; }
  
  public void add(String name, Command command) {
    commands.put(name, command);
  }
  
  public void execute(String cmdLine) throws Exception {
    CommandInput cmdInput = new CommandInput(cmdLine, true) ;
    Command command = commands.get(cmdInput.getCommand());
    if(command == null) {
      throw new Exception("Unkown command " + cmdInput.getCommand()) ;
    }
    command.execute(this, cmdInput);
  }
}