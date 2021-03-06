package com.neverwinterdp.registry.event;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.neverwinterdp.registry.ErrorCode;
import com.neverwinterdp.registry.Node;
import com.neverwinterdp.registry.Registry;
import com.neverwinterdp.registry.RegistryException;

public class RegistryListener {
  private Registry registry;
  private TreeMap<String, NodeWatcherWrapper> watchers = new TreeMap<String, NodeWatcherWrapper>() ;
  private boolean closed = false;
  
  public RegistryListener(Registry registry) {
    this.registry = registry;
  }

  public Registry getRegistry() { return this.registry ; }
  
  public TreeMap<String, NodeWatcherWrapper> getWatchers() { return this.watchers; }
  
  public void watch(String path, NodeWatcher nodeWatcher, boolean persistent) throws RegistryException {
    if(registry.exists(path)) {
      watchModify(path, nodeWatcher, persistent);
      return;
    }
    
    String key = createKey(path, nodeWatcher);
    NodeWatcherWrapper wrapper = null ;
    if(!persistent) {
      wrapper = new OneTimeNodeWatcher(key, nodeWatcher);
    } else {
      wrapper = new PersistentModifyNodeWatcher(key, nodeWatcher);
    }
    registry.watchExists(path, wrapper);
    watchers.put(key, wrapper) ;
  }
  
  public void watch(String path, NodeWatcher nodeWatcher) throws RegistryException {
    watch(path, nodeWatcher, true) ;
  }
  
  public void watchModify(String path, NodeWatcher nodeWatcher, boolean persistent) throws RegistryException {
    String key = createKey(path, nodeWatcher);
    NodeWatcherWrapper wrapper = null ;
    if(!persistent) {
      wrapper = new OneTimeNodeWatcher(key, nodeWatcher);
    } else {
      wrapper = new PersistentModifyNodeWatcher(key, nodeWatcher);
    }
    registry.watchModify(path, wrapper);
    watchers.put(key, wrapper) ;
  }
  
  public void watchChildren(final String path, final NodeWatcher nodeWatcher, final boolean persistent, boolean waitIfNotExist) throws RegistryException {
    if(registry.exists(path)) {
      watchChildren(path, nodeWatcher, persistent);
    } else {
      NodeWatcher createWatcher = new NodeWatcher() {
        @Override
        public void onEvent(NodeEvent event) {
          if(event.getType() == NodeEvent.Type.CREATE) {
            try {
              watchChildren(path, nodeWatcher, persistent);
            } catch (RegistryException e) {
              e.printStackTrace();
            }
          }
        }
      };
      watch(path, createWatcher) ;
      return;
    }
  }
  
  public void watchChildren(String path, NodeWatcher nodeWatcher, boolean persistent) throws RegistryException {
    String key = createKey(path, nodeWatcher);
    NodeWatcherWrapper wrapper = null ;
    if(!persistent) {
      wrapper = new OneTimeNodeWatcher(key, nodeWatcher);
    } else {
      wrapper = new PersistentChildrenNodeWatcher(key, nodeWatcher);
    }
    registry.watchChildren(path, wrapper);
    watchers.put(key, wrapper) ;
  }
  
  public void watchHeartbeat(String path, NodeWatcher nodeWatcher) throws RegistryException {
    watch(path + "/heartbeat", nodeWatcher, true) ;
  }
  
  public void watchHeartbeat(Node node, NodeWatcher nodeWatcher) throws RegistryException {
    watch(node.getPath() + "/heartbeat", nodeWatcher, true) ;
  }
  
  public void close() { closed = true; }
  
  public void dump(Appendable out) throws IOException {
    for(Map.Entry<String, NodeWatcherWrapper> entry : watchers.entrySet()) {
      out.append(entry.getKey()).append("\n");
    }
  }
  
  private String createKey(String path, NodeWatcher watcher) throws RegistryException {
    String key =  path + "[" + watcher.getClass().getName() + "#" + watcher.hashCode() + "]";
    if(watchers.containsKey(key)) {
      throw new RegistryException(ErrorCode.Unknown, "Already watch " + path + " with the watcher " + watcher.getClass()) ;
    }
    return key;
  }
  
  abstract class PersistentNodeWatcher extends NodeWatcherWrapper {
    String key ;
    
    PersistentNodeWatcher(String key, NodeWatcher nodeWatcher) { 
      super(nodeWatcher); 
      this.key = key;
    }
    
    @Override
    public void onEvent(NodeEvent event) throws Exception {
      if(closed) return;
      try {
        if(isComplete()) {
          watchers.remove(key);
          return;
        }
        doWatch(event);
      } catch(RegistryException ex) {
        if(ex.getErrorCode() != ErrorCode.NoNode) {
          System.err.println("watch " + event.getPath() + ": " + ex.getMessage());
        } else {
          watchers.remove(key);
        }
      }
      nodeWatcher.onEvent(event);
    }
    
    abstract protected void doWatch(NodeEvent event) throws RegistryException ;
  }
  
  class PersistentModifyNodeWatcher extends PersistentNodeWatcher {
    PersistentModifyNodeWatcher(String key, NodeWatcher nodeWatcher) {
      super(key, nodeWatcher);
    }
    
    protected void doWatch(NodeEvent event) throws RegistryException {
      registry.watchModify(event.getPath(), this);
    }
  }
  
  class PersistentChildrenNodeWatcher extends PersistentNodeWatcher {
    PersistentChildrenNodeWatcher(String key, NodeWatcher nodeWatcher) {
      super(key, nodeWatcher);
    }
    
    protected void doWatch(NodeEvent event) throws RegistryException {
      registry.watchChildren(event.getPath(), this);
    }
  }
  
  class OneTimeNodeWatcher extends NodeWatcherWrapper {
    private String      key;
    
    OneTimeNodeWatcher(String key, NodeWatcher nodeWatcher) {
      super(nodeWatcher);
      this.key = key;
    }
    
    @Override
    public void onEvent(NodeEvent event) throws Exception {
      if(closed) return;
      nodeWatcher.onEvent(event);
      watchers.remove(key);
    }
  }
}