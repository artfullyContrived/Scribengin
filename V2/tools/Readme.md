#Install Python#
For MacOS , dowload the latest python 2.7 from https://www.python.org/downloads/

#Configure PyDev for Development#
http://pydev.org/manual_101_install.html

#Install dependencies#
```sudo ./cluster/setup.sh```

#Run unit tests#
```
./cluster/runTests.sh
```

#Run Integration tests#
 You must bring up the Scribengin cluster first, directions found [here](https://github.com/DemandCube/Scribengin/tree/dev/master/V2/docker/scribengin)  
```
./cluster/runIntegrationTests.sh
```

#Usage#
```
Usage: clusterCommander.py [OPTIONS] COMMAND1 [ARGS]... [COMMAND2
                           [ARGS]...]...

Options:
  --debug / --no-debug  Turn debugging on
  --logfile TEXT        Log file to write to
  --help                Show this message and exit.

Commands:
  kafka             Kafka commands
  kafkafailure      Failure Simulation for Kafka
  monitor           Monitor Cluster status
  status            Get Cluster status
  zookeeper         Zookeeper commands
  zookeeperfailure  Failure Simulation for Zookeeper
  
###########
Usage: clusterCommander.py monitor [OPTIONS]

  Monitor Cluster status

Options:
  --update-interval INTEGER  Time interval (in seconds) to wait between
                             updating cluster status
  --help                     Show this message and exit.
  

###########
Usage: clusterCommander.py kafka [OPTIONS]

  Kafka commands

Options:
  --restart                    restart kafka brokers
  --start                      start kafka brokers
  --stop                       stop kafka brokers
  --force-stop                 kill -9 kafka on brokers
  --clean                      Clean old kafka data
  --brokers TEXT               Which kafka brokers to effect (command
                               separated list)
  --wait-before-start INTEGER  Time to wait before restarting kafka server
                               (seconds)
  --wait-before-kill INTEGER   Time to wait before force killing Kafka process
                               (seconds)
  --help                       Show this message and exit.

###########
Usage: clusterCommander.py kafkafailure [OPTIONS]

  Failure Simulation for Kafka

Options:
  --failure-interval INTEGER      Time interval (in seconds) to fail server
  --wait-before-start INTEGER     Time to wait (in seconds) before starting
                                  server
  --servers TEXT                  Servers to effect.  Command separated list
                                  (i.e. --servers zk1,zk2,zk3)
  --min-servers INTEGER           Minimum number of servers that must stay up
  --servers-to-fail-simultaneously INTEGER
                                  Number of servers to kill simultaneously
  --kill-method [restart|kill|random]
                                  Server kill method.  Restart is clean, kill
                                  uses kill -9, random switches randomly
  --initial-clean                 If enabled, will run a clean operation
                                  before starting the failure simulation
  --junit-report TEXT             If set, will write the junit-report to the
                                  specified file
  --help                          Show this message and exit.

###########
Usage: clusterCommander.py zookeeper [OPTIONS]

  Zookeeper commands

Options:
  --restart                    restart ZK nodes
  --start                      start ZK nodes
  --stop                       stop ZK nodes
  --force-stop                 kill -9 ZK on brokers
  --clean                      Clean old ZK data
  --zk-servers                 Which ZK nodes to effect (command separated
                               list)
  --wait-before-start INTEGER  Time to wait before starting ZK server
                               (seconds)
  --wait-before-kill INTEGER   Time to wait before force killing ZK process
                               (seconds)
  --help                       Show this message and exit.

###########
Usage: clusterCommander.py zookeeperfailure [OPTIONS]

  Failure Simulation for Zookeeper

Options:
  --failure-interval INTEGER      Time interval (in seconds) to fail server
  --wait-before-start INTEGER     Time to wait (in seconds) before starting
                                  server
  --servers TEXT                  Servers to effect.  Command separated list
                                  (i.e. --servers zk1,zk2,zk3)
  --min-servers INTEGER           Minimum number of servers that must stay up
  --servers-to-fail-simultaneously INTEGER
                                  Number of servers to kill simultaneously
  --kill-method [restart|kill|random]
                                  Server kill method.  Restart is clean, kill
                                  uses kill -9, random switches randomly
  --initial-clean                 If enabled, will run a clean operation
                                  before starting the failure simulation
  --junit-report TEXT             If set, will write the junit-report to the
                                  specified file
  --help                          Show this message and exit.

###########
Usage: clusterCommander.py status [OPTIONS]

  Get Cluster status

Options:
  --role TEXT  Which role to check on (i.e. kafka, zookeeper, hadoop-master,
               hadoop-worker)
  --help       Show this message and exit.

```