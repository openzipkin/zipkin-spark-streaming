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
package zipkin.sparkstreaming.job;

import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import zipkin.sparkstreaming.SparkStreamingJob;
import zipkin.sparkstreaming.StreamFactory;
import zipkin.sparkstreaming.stream.kafka.KafkaStreamFactory;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = ZipkinSparkStreamingJob.class,
    properties = {
        // update before running test
        "zipkin.sparkstreaming.stream.kafka.zookeeper:1.1.1.1:2222",
        "zipkin.sparkstreaming.stream.kafka.zkConnectionPath:abc",
    }
)
public class ZipkinSparkStreamingJobIntegrationTest {

  @Autowired SparkStreamingJob job;

  @Autowired StreamFactory factory;

  @After public void close() throws IOException {
    if (job != null) job.close();
  }

  @Test public void wiresJob() {
    assertThat(job).isNotNull();
  }

  @Test public void wireStreamFactory() {
    assertThat(factory).isInstanceOf(KafkaStreamFactory.class);
  }

}
