package com.neverwinterdp.scribengin;

import java.io.Closeable;
import java.io.IOException;

/**
 * A reader of a source. 
 */
public interface SourceReader extends Closeable {

  //See http://docs.oracle.com/javase/7/docs/api/java/io/InputStream.html#read(byte[])
  public Stream read(byte[] b) throws IOException;

}
