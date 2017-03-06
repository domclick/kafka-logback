This is a logback appender for Kafka (0.10.1.0 kafka-client used)
This appender expects topic, bootstrapServers, valueSerializer and layout as mandatory.
You could also supply additional kafka customProps in &lt;customProps&gt; tag in the following manner
&lt;customProps&gt;key1|value1,key2|value2&lt;/customProps&gt;
<p>
We recommend wrapping this appender inside AsyncAppender in order not to block your application (consider setting neverBlock set to true).
This appender ships with CustomJsonLayout, which extends JsonLayout from logback. It inherits all its properties and adds its own in the same manner.
List of properties added: includeLineNumber, includeClassName, includeMethodName, includeHost, includeFileName all set to true by default.
You can also add any constant fields via additionalFields (see sample config below).
<p>
Do not forget to add &lt;includeCallerData&gt;true&lt;/includeCallerData&gt; to AsyncAppender in case you need caller data (method, class, file, line number).
Kafka appender could be used with any ither alyout rather than Json, just think about proper kafka value Serializer.
<p>
Sample configuration
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <appender name="KAFKA" class="ru.sberned.kafkalogback.KafkaAppender">
        <topic><your_topic_name></topic>
        <bootstrapServers><server1>,<server2></bootstrapServers>
        <valueSerializer>org.apache.kafka.common.serialization.StringSerializer</valueSerializer>
        <failOnStartup>false</failOnStartup>
        <customProp>acks|all</customProp>
        <layout class="ru.sberned.kafkalogback.CustomJsonLayout">
            <timestampFormat>yyyy-MM-dd'T'HH:mm:ssZ</timestampFormat>
            <jsonFormatter class="ch.qos.logback.contrib.jackson.JacksonJsonFormatter"/>
            <additionalField>environment|${KAFKA_ENVIRONMENT:-dev}</additionalField>
        </layout>
    </appender>

    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <neverBlock>true</neverBlock>
        <includeCallerData>true</includeCallerData>
        <appender-ref ref="KAFKA" />
    </appender>

    <!-- Do not sent kafka client logs to kafka. This way you could fill all the queue of AsyncAppender-->
    <logger name="org.apache.kafka" level="INFO" additivity="false">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </logger>

    <root level="TRACE">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
        <appender-ref ref="ASYNC" />
    </root>

</configuration>