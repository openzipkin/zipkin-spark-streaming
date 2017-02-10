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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.spark.api.java.JavaRDD;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import zipkin.BinaryAnnotation;
import zipkin.Codec;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.autoconfigure.sparkstreaming.ZipkinSparkStreamingAutoConfiguration;
import zipkin.sparkstreaming.Consumer;
import zipkin.sparkstreaming.SparkStreamingJob;
import zipkin.sparkstreaming.StreamFactory;
import zipkin.sparkstreaming.autoconfigure.stream.kafka.ZipkinKafkaStreamFactoryAutoConfiguration;

import static java.util.Arrays.asList;

@SpringBootApplication
@Import({
    ZipkinSparkStreamingAutoConfiguration.class,
    ZipkinSparkStreamingJob.DummyConfiguration.class,
    ZipkinKafkaStreamFactoryAutoConfiguration.class
})
public class ZipkinSparkStreamingJob {

  public static void main(String[] args) throws UnsupportedEncodingException {
    new SpringApplicationBuilder(ZipkinSparkStreamingJob.class)
        .properties("zipkin.sparkstreaming.spark-jars", pathToUberJar())
        .run(args)
        .getBean(SparkStreamingJob.class).awaitTermination();
  }

  // We need to use eventually us auto-configuration for StreamFactory and Consumer.
  // This is an example, that seeds a single span (then loops forever since no more spans arrive).
  @Configuration
  static class DummyConfiguration {
    @Bean Consumer consumer() {
      return trace -> System.err.println(trace);
    }
  }

  static Span span(long traceId) {
    Endpoint e = Endpoint.builder().serviceName("service").ipv4(127 << 24 | 1).port(8080).build();
    return Span.builder().traceId(traceId).id(traceId)
        .timestamp(System.currentTimeMillis() * 1000).duration(200L)
        .name("hello").addBinaryAnnotation(BinaryAnnotation.create("lc", "", e))
        .build();
  }

  static String pathToUberJar() throws UnsupportedEncodingException {
    URL jarFile = ZipkinSparkStreamingJob.class.getProtectionDomain().getCodeSource().getLocation();
    return URLDecoder.decode(jarFile.getPath(), "UTF-8");
  }
}
