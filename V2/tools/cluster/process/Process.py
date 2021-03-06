from os.path import expanduser, join
from tabulate import tabulate
import paramiko


class Process(object):
  def __init__(self, role, hostname, homeDir, processIdentifier, sshKeyPath=join(expanduser("~"),".ssh/id_rsa")):
    self.role = role ;
    self.hostname = hostname;
    self.homeDir = homeDir;
    self.processIdentifier = processIdentifier;
    self.sshKeyPath = sshKeyPath
  
  def sshExecute(self, command, user = "neverwinterdp"):
    """
    SSH onto a machine, execute a command
    Returns [stdout,stderr]
    """
    key = paramiko.RSAKey.from_private_key_file(self.sshKeyPath)
    c = paramiko.SSHClient()
    c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    c.connect( hostname = self.hostname, username = user, pkey = key )
    stdin, stdout, stderr = c.exec_command(command)
    
    stdout = stdout.read()
    stderr = stderr.read()
    c.close()
    
    return stdout,stderr
  
  def getReportDict(self):
    running = "Running"
    if not self.isRunning():
      running = "None"
      
    return  {
            "Role" : self.role,
            "Hostname": self.hostname,
            "HomeDir" : self.homeDir,
            "ProcessIdentifier" : self.processIdentifier,
            "Status" : running,
            "processID" : self.getRunningPid()
            }
  
  def getReport(self):
    report = self.getReportDict()
    return tabulate([report.values()], headers=report.keys())
    
  def report(self):
    print self.getReport()
    
  
  def isRunning(self):
    return len(self.getRunningPid()) > 0
  
  def getRole(self):
    return self.role
    
  def kill(self):
    pids = self.getRunningPid()
    for pid in pids.split(","):
      self.sshExecute("kill -9 "+pid)
    
  def getRunningPid(self):
    command = "ps ax | grep -i '"+self.processIdentifier+"' | grep java | grep -v grep | awk '{print $1}'"
    stdout,stderr = self.sshExecute(command)
    return stdout.strip().replace("\n",",")
  
  def start(self):
    pass
  
  def shutdown(self):
    pass
  
  def clean(self):
    pass
  

############

class KafkaProcess(Process):
  def __init__(self, hostname):
    Process.__init__(self, "kafka", hostname, "/opt/kafka", "kafka")

  def start(self):
    return self.sshExecute(join(self.homeDir, "bin/kafka-server-start.sh")+" -daemon "+ join(self.homeDir, "config/server.properties"))
        
  def shutdown(self):
    return self.sshExecute( join(self.homeDir, "bin/kafka-server-stop.sh") )
  
  def clean(self):
    return self.sshExecute("rm -rf "+join(self.homeDir, "data")+ " && rm -rf "+join(self.homeDir, "logs"))
    
############

class ZookeeperProcess(Process):
  def __init__(self, hostname):
    Process.__init__(self, 'zookeeper',hostname, "/opt/zookeeper", 'QuorumPeerMain')

  def start(self):
    return self.sshExecute("ZOO_LOG4J_PROP='INFO,ROLLINGFILE' ZOO_LOG_DIR="+join(self.homeDir,"logs")+" "+join(self.homeDir, "bin/zkServer.sh")+ " start")
    
  def shutdown(self):
    return self.sshExecute( join(self.homeDir,"bin/zkServer.sh")+ " stop")
  
  def clean(self):
    return self.sshExecute("rm -rf "+join(self.homeDir, "data")+ " && rm -rf "+join(self.homeDir, "logs")+" && rm -rf "+ join(self.homeDir, "zookeeper.out"))

############

class HadoopWorkerProcess(Process):
  def __init__(self, hostname):
    Process.__init__(self, 'hadoop-worker',hostname, "/opt/hadoop", 'Hadoop Worker Identifier')

  def start(self):
    print "TODO: shutdown worker"
    
  def shutdown(self):
    print("TODO: call $homeDir/bin/shutdown.sh");
  
  def clean(self):
    print "TODO: clean hdfs"

############

class HadoopMasterProcess(Process):
  def __init__(self, hostname):
    Process.__init__(self, 'hadoop-master',hostname, "/opt/hadoop", 'Hadoop Master Identifier')

  def start(self):
    print "TODO shutdown master"
    
  def shutdown(self):
    print("TODO: call $homeDir/bin/shutdown.sh");
    
  def clean(self):
    print "TODO: clean"