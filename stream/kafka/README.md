# stream-kafka

## KafkaStream

This stream ingests Spans form a Kafka topic advertised by Zookeeper, using Spark Kafka libraries.

Kafka messages should contain a list of spans in json or TBinaryProtocol big-endian encoding.
Details on message encode is available [here](https://github.com/openzipkin/zipkin/blob/master/zipkin-collector/kafka/README.md#encoding-spans-into-kafka-messages)

## Configuration
KafkaStream can be used as a library, where attributes are set via
`KafkaStreamFactory.Builder`. It is more commonly enabled with Spring via autoconfiguration.

Here are the relevant setting and a short description. All properties
have a prefix of "zipkin.sparkstreaming.stream.kafka"

Attribute | Property | Description
--- | --- | ---
topic | topic | Kafka topic zipkin spans will be consumed from. Defaults to "zipkin"
zookeeper | zookeeper | Comma separated host:port pairs, each corresponding to a Zookeeper server. e.g. "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002". No default
zkConnectionPath | zkConnectionPath | Connection path for Zookeeper. Used as suffix for zk connection string. No default
zkSessionTimeout | zkSessionTimeout | Zookeeper session timeout. Defaults to “10000”