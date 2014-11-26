package com.neverwinterdp.scribengin.source;

import org.apache.log4j.Logger;

public interface SourceStream {

  static final Logger logger = Logger.getLogger(SourceStream.class);

  public SourceStreamDescriptor getDescriptor();

  public SourceStreamReader getReader(String name);
}
