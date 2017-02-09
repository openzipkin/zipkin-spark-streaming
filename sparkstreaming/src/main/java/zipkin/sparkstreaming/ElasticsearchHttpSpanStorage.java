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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.Span;
import zipkin.storage.Callback;
import zipkin.storage.elasticsearch.http.ElasticsearchHttpStorage;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

public class ElasticsearchHttpSpanStorage implements TraceConsumer, Cloneable {

  String _esUrl = "http://XXX.us-east-1.es.amazonaws.com";
  String _esIndexPrefix = "zipkin";
  int _esIndexReplicas = 1;
  int _esIndexShards = 10;

  private static final Logger logger = LoggerFactory.getLogger(ElasticsearchHttpSpanStorage.class);

  ElasticsearchHttpStorage storage;

  public ElasticsearchHttpSpanStorage() {
    storage = ElasticsearchHttpStorage.builder()
        .hosts(Arrays.asList(_esUrl))
        .index(_esIndexPrefix)
        .indexReplicas(_esIndexReplicas)
        .indexShards(_esIndexShards)
        .build();

    if ( !storage.check().ok ) {
      throw new InvalidParameterException(
          " ES storage error : " + storage.check().exception.getMessage());
    }
  }

  /**
   * Provides a response after attempting to write spans to storage.
   * @return Callback<Void> that logs status on both success and failure
   */
  public static Callback<Void> acceptSpansCallback() {
    return new Callback<Void>() {
      @Override
      public void onSuccess(Void value) {
        logger.debug("Successfully wrote to ES", value);
      }

      @Override
      public void onError(Throwable t) {
        logger.error("Failed to write spans to ES", t);
      }
    };
  }

  @Override
  public void accept(Iterable<Span> trace) {

    List<Span> spanList = new ArrayList<>();
    for (Span span : trace) {
      spanList.add(span);
    }

    if (!spanList.isEmpty()) {
      storage.asyncSpanConsumer().accept(spanList, acceptSpansCallback());
    }
  }

  public boolean checkStorage() {
    return storage.check().ok;
  }
}
