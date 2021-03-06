package com.neverwinterdp.command.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.neverwinterdp.jetty.JettyServer;
import com.neverwinterdp.registry.Registry;
import com.neverwinterdp.registry.RegistryException;
import com.neverwinterdp.vm.client.VMClient;
import com.neverwinterdp.vm.client.shell.Shell;

public class CommandServletUnitTest {
  protected static JettyServer commandServer;
  protected static int port = 8181;

  static Shell shell;
  static VMClient vmClient;
  
  @BeforeClass
  public static void setup() throws Exception {
    CommandServerTestBase.setup();

    Registry registry = CommandServerTestBase.getNewRegistry();
    try {
      registry.connect();
    } catch (RegistryException e) {
      e.printStackTrace();
    }
    
    //Point our context to our web.xml we want to use for testing
    WebAppContext webapp = new WebAppContext();
    webapp.setResourceBase(CommandServerTestBase.getCommandServerFolder());
    webapp.setDescriptor(CommandServerTestBase.getCommandServerXml());
    
    //Bring up commandServer using that context
    commandServer = new JettyServer(port, CommandServlet.class);
    commandServer.setHandler(webapp);
    commandServer.start();
  }
  
  
  @AfterClass
  public static void teardown() throws Exception{
    //Uncomment this line if you want the server to not exit
    //commandServer.join();

    commandServer.stop();
    CommandServerTestBase.teardown();
  }
  
  
  @Test
  public void testCommandServletListVMs() throws InterruptedException, UnirestException{
    HttpResponse<String> resp = Unirest.post("http://localhost:"+Integer.toString(port))
           .field("command", "vm info")
           .asString();
    
    //assertEquals("command run: "+"listvms", resp.getBody());
    //assertEquals(CommandServerTestBase.expectedListVMResponse, resp.getBody());
    assertFalse(resp.getBody().isEmpty());
  }
  
  @Test
  public void testCommandServletBadCommand() throws InterruptedException, UnirestException{
    String badCommand = "xxyyzz";
    HttpResponse<String> resp = Unirest.post("http://localhost:"+Integer.toString(port))
           .field("command", badCommand)
           .asString();
    
    assertEquals("", resp.getBody());
  }
  
  @Test
  public void testCommandServletNoCommand() throws UnirestException{
    HttpResponse<String> resp = Unirest.post("http://localhost:"+Integer.toString(port))
        .asString();
    
    assertEquals(CommandServlet.noCommandMessage, resp.getBody());
  }
  
  
  @Test
  public void testCommandServletStopScribeMaster() throws UnirestException{
    HttpResponse<String> x = Unirest.post("http://localhost:"+Integer.toString(port))
        .field("command", "registry dump")
        .asString();
    System.err.println("");
    System.err.println("");
    System.err.println("");
    System.err.println("");
    System.err.println(x.getBody());
    
    HttpResponse<String> resp = Unirest.post("http://localhost:"+Integer.toString(port))
           .field("command", "scribengin server")
           .field("stop-master", "vm")
           .asString();
    System.err.println("");
    System.err.println("");
    System.err.println("");
    System.err.println("");
    System.err.println(resp.getBody());
    System.err.println("");
    System.err.println("");
    System.err.println("");
    System.err.println("");
    x = Unirest.post("http://localhost:"+Integer.toString(port))
        .field("command", "registry dump")
        .asString();
    System.err.println(x.getBody());
    System.err.println("");
    System.err.println("");
    System.err.println("");
    System.err.println("");
    
    //assertEquals("", resp.getBody());
    
  }
}
