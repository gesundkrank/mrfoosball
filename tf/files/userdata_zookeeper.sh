#!/bin/bash -eux

amazon-linux-extras install -y java-openjdk11

wget https://www-eu.apache.org/dist/zookeeper/stable/apache-zookeeper-${ZOOKEEPER_VERSION}-bin.tar.gz
tar -xzf apache-zookeeper-${ZOOKEEPER_VERSION}-bin.tar.gz
mv apache-zookeeper-${ZOOKEEPER_VERSION}-bin /opt/zookeeper-${ZOOKEEPER_VERSION}
ln -s /opt/zookeeper-${ZOOKEEPER_VERSION} /opt/zookeeper
mkdir /var/lib/zookeeper

cat <<EOF >/opt/zookeeper/conf/zoo.cfg
tickTime=2000
dataDir=/var/lib/zookeeper
clientPort=2181
EOF

/opt/zookeeper/bin/zkServer.sh start
