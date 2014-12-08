package com.neverwinterdp.scribengin.client.shell;

import com.neverwinterdp.scribengin.client.ScribenginClient;
import com.neverwinterdp.vm.client.shell.Command;
import com.neverwinterdp.vm.client.shell.CommandInput;
import com.neverwinterdp.vm.client.shell.Shell;
import com.neverwinterdp.vm.client.shell.SubCommand;

public class ScribenginCommand extends Command {
  public ScribenginCommand() {
    add("master", new ListMasterDescriptor()) ;
    add("registry-dump", new RegistryDump()) ;
  }
  
  static public class ListMasterDescriptor extends SubCommand {
    @Override
    public void execute(Shell shell, CommandInput cmdInput) throws Exception {
      ScribenginClient client = ((ScribenginShell)shell).getScribenginClient();
    }
  }
  
  
  static public class RegistryDump extends SubCommand {
    @Override
    public void execute(Shell shell, CommandInput cmdInput) throws Exception {
      ScribenginClient client = ((ScribenginShell)shell).getScribenginClient();
    }
  }
}
