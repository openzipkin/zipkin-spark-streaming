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
package zipkin.sparkstreaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.serializer.DefaultDecoder;
import org.apache.commons.lang.StringUtils;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KafkaStream implements MessageStreamFactory, Cloneable {

  String _kafkaTopic = "zipkin";
  String _zkHosts = "abc:2181,def:2181";
  String _zkConnectPath = "abc";
  int _zkSessionTimeout = 10000;

  private Map<String, String> kafkaParams = new HashMap<>();

  private Map<String, String> getKafkaParams() {
    kafkaParams.put("metadata.broker.list", getBrokers());
    return Collections.unmodifiableMap(kafkaParams);
  }

  private String getBrokers() {

    try {
      ZooKeeper zkClient =
          new ZooKeeper(_zkHosts + "/" + _zkConnectPath, _zkSessionTimeout,
              new NoOpWatcher());
      List<String> ids = zkClient.getChildren("/brokers/ids", false);
      ObjectMapper objectMapper = new ObjectMapper();

      List<String> brokerConnections = new ArrayList<>();
      for (String id : ids) {
        brokerConnections.add(getBrokerInfo(zkClient, objectMapper, id));
      }

      return StringUtils.join(brokerConnections, ",");

    } catch (Exception e) {
      throw new InvalidParameterException("Error loading brokers from zookeeper");
    }

  }

  /**
   * Builds string to create KafkaParams for Spark job
   * @param zkClient ZooKeeper client with predefined configurations
   * @param om ObjectMapper used to read zkClient's children (brokers)
   * @param id broker id
   * @return "host:port" string
   */
  private String getBrokerInfo(ZooKeeper zkClient, ObjectMapper om, String id) {
    try {
      Map map = om.readValue(
          zkClient.getData("/brokers/ids/" + id, false, null), Map.class);
      String host = String.valueOf(map.get("host"));
      String port = String.valueOf(map.get("port"));
      return host + ":" + port;
    } catch (Exception e) {
      throw new InvalidParameterException("Error reading zkClient's broker id's");
    }
  }

  @Override
  public JavaDStream<byte[]> create(JavaStreamingContext jsc) {

    return KafkaUtils.createDirectStream(
        jsc,
        byte[].class,
        byte[].class,
        DefaultDecoder.class,
        DefaultDecoder.class,
        getKafkaParams(),
        Collections.singleton(_kafkaTopic))
        .map(m -> m._2); // get value
  }

  class NoOpWatcher implements Watcher {

    private Logger logger = LoggerFactory.getLogger(NoOpWatcher.class);

    @Override
    public void process(WatchedEvent event) {
      logger.debug(event.toString());
    }
  }
}
