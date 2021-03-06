#!/bin/bash


BIN_DIR=`dirname "$0"`
BIN_DIR=`cd "$BIN_DIR"; pwd`

HOST_IP="127.0.0.1"

OS=`uname`
if [[ "$OS" == 'Linux' ]]; then
   OS='linux'
elif [[ "$OS" == 'FreeBSD' ]]; then
   platform='freebsd'
elif [[ "$OS" == 'Darwin' ]]; then
   platform='macos'
   HOST_IP=$(boot2docker ip)
fi

function has_opt() {
  OPT_NAME=$1
  shift
  #Par the parameters
  for i in "$@"; do
    if [[ $i == $OPT_NAME* ]] ; then
      echo "true"
      return
    fi
  done
  echo "false"
}

function get_opt() {
  OPT_NAME=$1
  DEFAULT_VALUE=$2
  shift
  
  #Par the parameters
  for i in "$@"; do
    index=$(($index+1))
    if [[ $i == $OPT_NAME* ]] ; then
      value="${i#*=}"
      echo "$value"
      return
    fi
  done
  echo $DEFAULT_VALUE
}

function h1() {
  echo ""
  echo "###########################################################################################################"
  echo "$@"
  echo "###########################################################################################################"
}

function build_image() {
  h1 "Build the os image with the preinstalled requirements"
  echo "Prepare the temporary configuration files"
  mkdir ./tmp
  
  if [ ! -d ../../release/build/release ] ; then
    ./scribengin.sh build
  fi
  
  #Move release/build/release to ./tmp
  cp -R -f ../../release/build/release ./tmp/release
  cp -R -f ../../tools/cluster ./tmp/cluster
  
  
  #Use existing key if it already exists
  if [ -e ~/.ssh/id_rsa ] && [ -e ~/.ssh/id_rsa.pub ]; then
    cat ~/.ssh/id_rsa > ./tmp/id_rsa
    cat ~/.ssh/id_rsa.pub > ./tmp/id_rsa.pub
  #Otherwise generate a new one
  else
    ssh-keygen -t rsa  -P "" -f tmp/id_rsa
  fi
  cat ~/.ssh/id_rsa.pub > ./tmp/authorized_keys

  docker build -t ubuntu:scribengin $BIN_DIR
  echo "Clean the temporary configuration files"
  rm -rf ./tmp
  
}

function clean_image() {
  h1 "Clean the images"
  docker rmi -f ubuntu:scribengin
}

function launch_containers() {
  h1 "Launch hadoop containers"
  docker run -d -p 22 -p 50070:50070 -p 9000:9000 -p 8030:8030 -p 8032:8032 -p 8088:8088 --privileged -h hadoop-master --name hadoop-master  ubuntu:scribengin
  
  
  NUM_KAFKA_BROKER=$(get_opt --kafka-server '3' $@)
  NUM_ZOOKEEPER_SERVER=$(get_opt --zk-server 1 $@)
  NUM_HADOOP_WORKER=$(get_opt --hadoop-worker 3 $@)
  
  NUM_SPARE_KAFKA_BROKER=$(get_opt --spare-kafka '0' $@)
  NUM_SPARE_ZOOKEEPER_SERVER=$(get_opt --spare-zookeeper 0 $@)
  NUM_SPARE_HADOOP_WORKER=$(get_opt --spare-hadoop 0 $@)
  
  h1 "Launch hadoop-worker containers"
  for (( i=1; i<="$NUM_HADOOP_WORKER"; i++ ))
  do
    NAME="hadoop-worker-"$i
    docker run -d -p 22 --privileged -h "$NAME" --name "$NAME" ubuntu:scribengin
  done

  h1 "Launch spare hadoop-worker containers"
  for (( i=1; i<="$NUM_SPARE_HADOOP_WORKER"; i++ ))
  do
    NAME="spare-hadoop-worker-"$i
    docker run -d -p 22 --privileged -h "$NAME" --name "$NAME" ubuntu:scribengin
  done
  
  h1 "Launch zookeeper containers"
  for (( i=1; i<="$NUM_ZOOKEEPER_SERVER"; i++ ))
  do
    NAME="zookeeper-"$i
    docker run -d -p 22 -p 2181 --privileged -h "$NAME" --name "$NAME"  ubuntu:scribengin
  done  

  h1 "Launch spare zookeeper containers"
  for (( i=1; i<="$NUM_SPARE_ZOOKEEPER_SERVER"; i++ ))
  do
    NAME="spare-zookeeper-"$i
    docker run -d -p 22 -p 2181 --privileged -h "$NAME" --name "$NAME"  ubuntu:scribengin
  done  

  h1 "Remove hadoop-master entry in the $HOME/.ssh/known_hosts"
  ssh-keygen -f "$HOME/.ssh/known_hosts" -R hadoop-master

  h1 "Launch kafka containers"
  for (( i=1; i<="$NUM_KAFKA_BROKER"; i++ ))
  do
    NAME="kafka-"$i
    docker run -d -p 22 -p 9092 --privileged -h "$NAME" --name "$NAME"  ubuntu:scribengin
  done
  
  h1 "Launch spare kafka containers"
  for (( i=1; i<="$NUM_SPARE_KAFKA_BROKER"; i++ ))
  do
    NAME="spare-kafka-"$i
    docker run -d -p 22 -p 9092 --privileged -h "$NAME" --name "$NAME"  ubuntu:scribengin
  done
  

  docker ps
}

function container_login() {
  h1 "Login to the instance $@"
  docker exec -i -t $1 bash 
}

function login_ssh_port() {
  docker port $1 22 | sed 's/.*://'
}

function do_ssh() {
  h1 "Login to the instance $@"
  arr=(${1//@/ })

  port=$(login_ssh_port ${arr[1]})
  ssh -p $port ${arr[0]}@$HOST_IP
}

function do_scp() {
  h1 "scp $@"

  HOST=$(echo "$@" | sed "s/.*@\(.*\):.*$/\1/")
  PORT=$(login_ssh_port $HOST)
  NEW_ARGS=$(echo "$@" | sed "s/@\(.*\):/@$HOST_IP:/")
  echo "NEW ARGS = $NEW_ARGS" 
  echo "scp -P $PORT $NEW_ARGS"
  scp -P $PORT $NEW_ARGS
}

function container_ids() {
  docker_ps_result=$(docker ps)
  entries=()
  while read -r line; do
    entries+=("$line")
  done <<< "$docker_ps_result"

  entries_len=${#entries[@]}
  IDS=""
  for(( i=1; i<${entries_len}; i++ ));
  do
    #extract the container id
    if (( i > 1 )); then
      IDS+=' '
    fi
    IDS+=$(echo ${entries[$i]} | cut -d' ' -f 1)
  done
  echo "$IDS"
}

function container_update_hosts() {
  h1 "Update /etc/hosts file"
  HOSTS=$'## scribengin server ##\n'
  HOSTS+=$'127.0.0.1 localhost\n\n'
  for container_id in $(container_ids); do
    #extract the container name
    container_name=$(docker inspect -f {{.Config.Hostname}} $container_id)
    container_domain=$(docker inspect -f {{.Config.Domainname}} $container_id)
    #extract the container ip
    container_ip=$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" $container_id)
    #extract the container running state
    container_running=$(docker inspect -f {{.State.Running}} $container_id)
    HOSTS+="$container_ip $container_name"
    HOSTS+=$'\n'
    #echo "container id = $container_id, container name = $container_name, container ip = $container_ip, container running = $container_running"
  done

  echo ""
  echo "Insert Content:"
  echo ""
  echo "-----------------------------------------------------------------------------------------------"
  echo "$HOSTS"
  echo "-----------------------------------------------------------------------------------------------"
  for container_id in $(container_ids); do
    #extract the container name
    container_name=$(docker inspect -f {{.Config.Hostname}} $container_id)
    echo "Update /etc/hosts for $container_name"
    ssh -o StrictHostKeyChecking=no -p $(login_ssh_port $container_id) root@$HOST_IP "echo '$HOSTS'  > /etc/hosts"
  done
}

function host_machine_update_hosts() {
  #Updating /etc/hosts file on host machine
  h1 "Updating /etc/hosts file of host machine"
  
  startString="##SCRIBENGIN CLUSTER START##"
  endString="##SCRIBENGIN CLUSTER END##"
  
  #Build entry to add to /etc/hosts by reading info from Docker
  hostString="$hostString\n$startString\n"
  for container_id in $(container_ids); do
    hostname=$(docker inspect -f '{{ .Config.Hostname }}' $container_id)
    hostString="$hostString$(docker inspect -f "{{ .NetworkSettings.IPAddress }}" $hostname)    $hostname\n"
  done
  hostString="$hostString$endString\n"
  
  if [ ! -f /etc/hosts ] ; then
    echo -e "\nERROR!!!\nYou don't have write permissions for /etc/hosts!!!\nManually add this to your /etc/hosts file:"
    echo -e "$hostString"
    return
  fi
  
  #Strip out old entry
  out=`sed "/$startString/,/$endString/d" /etc/hosts`
  #write new hosts file
  echo -e "$out\n$hostString" > /etc/hosts
  echo -e "$hostString"
}

function container_clean() {
  for container_id in $(container_ids); do
    container_name=$(docker inspect -f {{.Config.Hostname}} $container_id)
    docker rm -f $container_id
    echo "Remove the instance $container_name"
  done
}

function host_sync() {
  ./scribengin.sh build
  
  ssh -o "StrictHostKeyChecking no" neverwinterdp@hadoop-master "mkdir /opt/scribengin"
  
  scp -r ../../release/build/release/*       neverwinterdp@hadoop-master:/opt/scribengin/
  scp -r ./bootstrap/post-install/hadoop     neverwinterdp@hadoop-master:/opt/
  scp -r ./bootstrap/post-install/kafka      neverwinterdp@hadoop-master:/opt/
  scp -r ./bootstrap/post-install/zookeeper  neverwinterdp@hadoop-master:/opt/
  scp -r ./bootstrap/post-install/cluster.sh neverwinterdp@hadoop-master:/opt/
  
  ssh -o "StrictHostKeyChecking no" neverwinterdp@hadoop-master "cd /opt && yes | ./cluster.sh sync"
}

function printUsage() {
  echo "Cluster command options: "
  echo "  Command image consists of the sub commands: "
  echo "    build                 : To build the ubuntu os image with the required components"
  echo "    clean                 : To remove the image"
  echo "  Command container consists of the sub commands: "
  echo "    run                   : To run the containers(hadoop, zookeeper, kafka...)"
  echo "    run --spare-zookeeper : Number of spare zookeeper servers to launch"
  echo "    run --spare-kafka     : Number of spare kafka servers to launch"
  echo "    run --spare-hadoop    : Number of spare hadoop-worker servers to launch"
  echo "    clean                 : To remove and destroy all the running containers"
  echo "    login                 : To login the given containeri name or id  with the root user"
  echo "    update-hosts          : To update the /etc/hosts in all the running containers"
  echo "  Other commands:"
  echo "    ssh                   : The ssh command use to resolve the container ssh port and login a container with ssh command"
  echo "    scp                   : The scp command use to resolve the container ssh port and copy the file/directory from or to a container"
  echo "    ip-route              : If you are running macos, use this command to route the 127.17.0.0 ip range to the boot2docker host. It allows to access the docker container directly from the MAC"
  echo "    host-sync             : Run this to run \"./scribengin.sh build\" and then sync the release folder and post-install with your cluster."
}

# get command
COMMAND=$1
shift

if [ "$COMMAND" = "image" ] ; then
  # get subcommand
  SUB_COMMAND=$1
  shift
  if [ "$SUB_COMMAND" = "build" ] ; then
    build_image $@
  elif [ "$SUB_COMMAND" = "clean" ] ; then
    clean_image $@
  else
    printUsage
  fi
elif [ "$COMMAND" = "container" ] ; then
  # get subcommand
  SUB_COMMAND=$1
  shift
  if [ "$SUB_COMMAND" = "clean" ] ; then
    container_clean $@
  elif [ "$SUB_COMMAND" = "run" ] ; then
    launch_containers $@
    container_update_hosts $@
  elif [ "$SUB_COMMAND" = "update-hosts" ] ; then
    container_update_hosts $@
  elif [ "$SUB_COMMAND" = "login" ] ; then
    container_login $@
  else
    printUsage
  fi
elif [ "$COMMAND" = "ssh" ] ; then
  do_ssh $@
elif [ "$COMMAND" = "scp" ] ; then
  do_scp $@
elif [ "$COMMAND" = "ip-route" ] ; then
  sudo route -n add 172.17.0.0/16 `boot2docker ip`
elif [ "$COMMAND" = "update-hosts" ] ; then
  host_machine_update_hosts
elif [ "$COMMAND" = "host-sync" ] ; then
  host_sync
else
  h1 "Docker Images"
  docker images
  h1 "Docker Running Containers"
  docker ps
  h1 "docker.sh usage"
  printUsage
fi

