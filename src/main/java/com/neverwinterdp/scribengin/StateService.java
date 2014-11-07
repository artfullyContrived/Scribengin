package com.neverwinterdp.scribengin;

/**
 * Scribes record their progress to a State Service.
 * 
 * ScribeMaster and Supervisor nodes are state-less and store all the state in a StateService.
 * 
 * */
public interface StateService {

}


class ZookeeperStateService implements StateService{
  
}

class HDFSStateService implements StateService{
  
}