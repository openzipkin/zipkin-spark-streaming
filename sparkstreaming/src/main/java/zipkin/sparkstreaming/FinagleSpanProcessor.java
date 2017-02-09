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

import org.apache.spark.streaming.api.java.JavaDStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.Annotation;
import zipkin.BinaryAnnotation;
import zipkin.Span;
import zipkin.internal.ApplyTimestampAndDuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FinagleSpanProcessor implements SpanProcessor {

  private static final Logger logger = LoggerFactory.getLogger(FinagleSpanProcessor.class);

  static boolean _dropFinagleFlushAnnotation = true;
  static boolean _addTimestampAndDuration = true;
  static boolean _trimBinaryAnnotationValue = true;
  static int _binAnnotationValueMaxSize = 2048;


  @Override
  public JavaDStream<Span> process(JavaDStream<Span> spanStream) {

    JavaDStream<Span> finagleFlushAnnotationDropped =
        spanStream.map(FinagleSpanProcessor::dropFinagleFlushAnnotation);

    JavaDStream<Span> binAnnotationValueTrimmed =
        finagleFlushAnnotationDropped.map(FinagleSpanProcessor::trimBinaryAnnotationValue);

    return binAnnotationValueTrimmed.map(FinagleSpanProcessor::applyTimeStampAndDuration);
  }

  static Span applyTimeStampAndDuration(Span span) {
    if (_addTimestampAndDuration) {
      return ApplyTimestampAndDuration.apply(span);
    }
    return span;
  }

  public static Span dropFinagleFlushAnnotation(Span span) {

    /* TODO
    if (_dropFinagleFlushAnnotation
        && span.annotations != null
        && !span.annotations.isEmpty()) {

      if ( span.annotations.stream()
          .filter(a -> a.value != null && a.value.equals("finagle.flush"))
          .findFirst()
          .isPresent()) {

        logger.info("Dropping finagle.flush for the span : " + span);

        List<Annotation> annotations = new ArrayList<>(span.annotations);
        annotations.removeIf(a -> a.value.equals("finagle.flush"));

        return span.toBuilder().annotations(annotations).build();
      }
    }
    */
    return span;
  }

  public static Span trimBinaryAnnotationValue(Span span) {

    if (_trimBinaryAnnotationValue
        && span.binaryAnnotations != null
        && !span.binaryAnnotations.isEmpty()) {

      List<BinaryAnnotation> newBinaryAnnotations = new ArrayList<>();

      for (BinaryAnnotation ba : span.binaryAnnotations) {
        newBinaryAnnotations.add(trimValue(ba));
      }

      return span.toBuilder().binaryAnnotations(newBinaryAnnotations).build();

    }
    return span;
  }

  public static BinaryAnnotation trimValue(BinaryAnnotation ba) {
    return ba.toBuilder().value(
        ba.value.length > _binAnnotationValueMaxSize ? Arrays
            .copyOfRange(ba.value, 0, _binAnnotationValueMaxSize) : ba.value
    ).build();
  }
}
