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
package zipkin.sparkstreaming.autoconfigure.adjuster.finagle;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin.sparkstreaming.Adjuster;
import zipkin.sparkstreaming.adjuster.finagle.FinagleIssue343Adjuster;

@Configuration
@EnableConfigurationProperties(ZipkinFinagleAdjusterProperties.class)
@ConditionalOnProperty(
    value = "zipkin.sparkstreaming.adjuster.finagle.enabled",
    havingValue = "true"
)
public class ZipkinFinagleAdjusterAutoConfiguration {

  @Bean
  Adjuster finagleAdjuster(ZipkinFinagleAdjusterProperties properties) {
    return properties.toBuilder().build();
  }

  @Bean
  @ConditionalOnProperty(
      value = "zipkin.sparkstreaming.adjuster.finagle.adjust-issue343",
      havingValue = "true"
  )
  Adjuster finagleIssue343Adjuster() {
    return FinagleIssue343Adjuster.create();
  }

}

