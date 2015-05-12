package com.neverwinterdp.scribengin.dataflow.test;

import com.beust.jcommander.ParametersDelegate;
import com.neverwinterdp.scribengin.client.shell.ScribenginShell;

public class S3DataflowTest extends DataflowTest {

  final static public String TEST_NAME = "s3-to-s3";

  @ParametersDelegate
  private DataflowSourceGenerator sourceGenerator = new HDFSDataflowSourceGenerator();

  @ParametersDelegate
  private DataflowSinkValidator sinkValidator = new HDFSDataflowSinkValidator();

  protected void doRun(ScribenginShell shell) throws Exception {
    sourceToSinkDataflowTest(shell, sourceGenerator, sinkValidator);
  }
}