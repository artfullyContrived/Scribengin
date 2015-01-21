package com.neverwinterdp.scribengin.source;

import org.apache.hadoop.fs.FileSystem;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.neverwinterdp.scribengin.hdfs.source.SourceImpl;

@Singleton
public class SourceFactory {
  @Inject
  private FileSystem fs;
  
  public Source create(SourceDescriptor descriptor) throws Exception {
    if("hdfs".equalsIgnoreCase(descriptor.getType())) {
      return new SourceImpl(fs, descriptor);
    }
    throw new Exception("Unknown source type " + descriptor.getType());
  }
  
  public Source create(SourceStreamDescriptor descriptor) throws Exception {
    if("hdfs".equalsIgnoreCase(descriptor.getType())) {
      return new SourceImpl(fs, descriptor);
    }
    throw new Exception("Unknown source type " + descriptor.getType());
  }
}