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
package zipkin.sparkstreaming.stream.kafka;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import zipkin.sparkstreaming.autoconfigure.stream.kafka.ZipkinKafkaStreamFactoryAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;

public class ZipkinKafkaStreamFactoryAutoConfigurationTest {

  AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

  @After
  public void close() {
    if (context != null) context.close();
  }

  @Test
  public void setPropertiesTest() {
    addEnvironment(context,
        "zipkin.sparkstreaming.stream.kafka.topic:topic",
        "zipkin.sparkstreaming.stream.kafka.zookeeper:zookeeper",
        "zipkin.sparkstreaming.stream.kafka.zk-connection-path:zkConnectionPath",
        "zipkin.sparkstreaming.stream.kafka.zk-session-timeout:" + 1);
    context.register(PropertyPlaceholderAutoConfiguration.class,
        ZipkinKafkaStreamFactoryAutoConfiguration.class);
    context.refresh();

    KafkaStreamFactory streamFactory = context.getBean(KafkaStreamFactory.class);
    assertThat(streamFactory.topic()).isEqualTo("topic");
    assertThat(streamFactory.zookeeper()).isEqualTo("zookeeper");
    assertThat(streamFactory.zkConnectionPath()).isEqualTo("zkConnectionPath");
    assertThat(streamFactory.zkSessionTimeout()).isEqualTo(1);
  }
}