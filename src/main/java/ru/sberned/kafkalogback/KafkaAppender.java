package ru.sberned.kafkalogback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * This appender expects topic, bootstrapServers, valueSerializer and layout as mandatory.
 * You could also supply additional kafka customProps in <customProps> tag in the following manner
 * <customProps>key1=value1,key2=value2</customProps>
 * <p>
 * Sample configuration
 * <?xml version="1.0" encoding="UTF-8" ?>
 * <configuration>
 *     <appender name="KAFKA" class="ru.sberned.kafkalogback.KafkaAppender">
 *         <topic>test-topic</topic>
 *         <bootstrapServers>localhost:2181</bootstrapServers>
 *         <layout class="ru.sberned.kafkalogback.CustomJsonLayout">
 *             <jsonFormatter class="ch.qos.logback.contrib.jackson.JacksonJsonFormatter"/>
 *         </layout>
 *     </appender>
 *     <root level="trace">
 *         <appender-ref ref="KAFKA" />
 *     </root>
 * </configuration>
 */
public class KafkaAppender extends AppenderBase<ILoggingEvent> {
    private String topic;
    private String bootstrapServers;
    private String valueSerializer;
    Producer<String, String> producer;
    private Layout<ILoggingEvent> layout;
    private List<String> customProps;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public void setValueSerializer(String valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public void setCustomProps(List<String> customProps) {
        this.customProps = customProps;
    }

    @Override
    public void start() {
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(bootstrapServers, "bootstrapServers must not be null");
        Objects.requireNonNull(valueSerializer, "valueSerializer must not be null");
        Objects.requireNonNull(layout, "layout must not be null");

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", valueSerializer);
        parseProperties(props);
        try {
            startProducer(props);
            super.start();
        } catch (Exception e) {
            addError("Unable to start Kafka Producer", e);
        }
    }

    void parseProperties(Properties properties) {
        if (customProps != null) {
            customProps.forEach(property -> {
                String[] p = property.split("\\|");
                if (p.length == 2) {
                    properties.put(p[0], p[1]);
                } else {
                    System.out.println("Unable to parse property string: " + property);
                }
            });
        }
    }

    // aka unit test friendly
    void startProducer(Properties props) throws Exception {
        producer = new KafkaProducer<>(props);
    }

    @Override
    public void stop() {
        super.stop();
        if (producer != null)
            producer.close();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (producer != null) {
            try {
                producer.send(new ProducerRecord<>(topic, String.valueOf(Math.random()), layout.doLayout(event))).get();
            } catch (Exception e) {
                addError("Unable to send message to Kafka", e);
            }
        }
    }

}
