package com.neverwinterdp.scribengin;

import java.io.Closeable;

public interface SinkWriter extends Closeable {

  //See http://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html#write(byte[])
  public void write(byte[] b, Stream stream);

}
