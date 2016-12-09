This appender expects topic, bootstrapServers, valueSerializer and layout as mandatory.
You could also supply additional kafka customProps in <customProps> tag in the following manner
<customProps>key1=value1,key2=value2</customProps>
<p>
Sample configuration
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
    <appender name="KAFKA" class="ru.sberned.kafkalogback.KafkaAppender">
        <topic>test-topic</topic>
        <bootstrapServers>localhost:2181</bootstrapServers>
        <layout class="ru.sberned.kafkalogback.CustomJsonLayout">
            <jsonFormatter class="ch.qos.logback.contrib.jackson.JacksonJsonFormatter"/>
        </layout>
    </appender>
    <root level="trace">
        <appender-ref ref="KAFKA" />
    </root>
</configuration>