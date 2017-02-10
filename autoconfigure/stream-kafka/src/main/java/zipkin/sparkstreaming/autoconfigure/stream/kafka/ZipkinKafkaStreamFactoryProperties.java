/**
 * Copyright 2017 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin.sparkstreaming.autoconfigure.stream.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import zipkin.sparkstreaming.stream.kafka.KafkaStreamFactory;

@ConfigurationProperties("zipkin.sparkstreaming.stream.kafka")
public class ZipkinKafkaStreamFactoryProperties {
  private String topic;
  private String zookeeper;
  private String zkConnectionPath;
  private Integer zkSessionTimeout;

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = "".equals(topic) ? null : topic;
  }

  public String getZookeeper() {
    return zookeeper;
  }

  public void setZookeeper(String zookeeper) {
    this.zookeeper = "".equals(zookeeper) ? null : zookeeper;
  }

  public String getZkConnectionPath() {
    return zkConnectionPath;
  }

  public void setZkConnectionPath(String zkConnectionPath) {
    this.zkConnectionPath = "".equals(zkConnectionPath) ? null : zkConnectionPath;
  }

  public Integer getZkSessionTimeout() {
    return zkSessionTimeout;
  }

  public void setZkSessionTimeout(Integer zkSessionTimeout) {
    this.zkSessionTimeout = zkSessionTimeout;
  }

  KafkaStreamFactory.Builder toBuilder() {
    KafkaStreamFactory.Builder result = KafkaStreamFactory.newBuilder();
    if (topic != null) result = result.topic(topic);
    if (zookeeper != null) result = result.zookeeper(zookeeper);
    if (zkConnectionPath != null) result = result.zkConnectionPath(zkConnectionPath);
    if (zkSessionTimeout != null) result = result.zkSessionTimeout(zkSessionTimeout);

    return result;
  }
}