package ru.sberned.kafkalogback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import lombok.Setter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * This appender expects topic, bootstrapServers, valueSerializer and layout as mandatory.
 * You could also supply additional kafka customProps in &lt;customProps&gt; tag in the following manner
 * &lt;customProps&gt;key1=value1,key2=value2&lt;/customProps&gt;
 * &lt;p&gt;
 * Sample configuration
 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; ?&gt;
 * &lt;configuration&gt;
 *     &lt;appender name=&quot;KAFKA&quot; class=&quot;ru.sberned.kafkalogback.KafkaAppender&quot;&gt;
 *         &lt;topic&gt;test-topic&lt;/topic&gt;
 *         &lt;bootstrapServers&gt;localhost:2181&lt;/bootstrapServers&gt;
 *         &lt;layout class=&quot;ru.sberned.kafkalogback.CustomJsonLayout&quot;&gt;
 *             &lt;jsonFormatter class=&quot;ch.qos.logback.contrib.jackson.JacksonJsonFormatter&quot;/&gt;
 *         &lt;/layout&gt;
 *     &lt;/appender&gt;
 *     &lt;root level=&quot;trace&quot;&gt;
 *         &lt;appender-ref ref=&quot;KAFKA&quot; /&gt;
 *     &lt;/root&gt;
 * &lt;/configuration&gt;
 */
@Setter
public class KafkaAppender extends AppenderBase<ILoggingEvent> {
    private String topic;
    private String bootstrapServers;
    private String valueSerializer;
    private boolean failOnStartup;
    Producer<String, String> producer;
    private Layout<ILoggingEvent> layout;
    private List<String> customProps = new ArrayList<>();

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
            if (failOnStartup) {
                addError("Unable to start Kafka Producer", e);
            } else {
                addWarn("Unable to start Kafka Producer", e);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (producer != null)
            producer.close();
    }

    public void addCustomProp(String customProp) {
        customProps.add(customProp);
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (producer != null) {
            try {
                producer.send(new ProducerRecord<>(topic, String.valueOf(Math.random()), layout.doLayout(event))).get();
            } catch (Exception e) {
                addWarn("Unable to send message to Kafka", e);
            }
        }
    }

    void parseProperties(Properties properties) {
        if (customProps != null) {
            customProps.forEach(property -> {
                String[] p = property.split("\\|");
                if (p.length == 2) {
                    properties.put(p[0], p[1]);
                } else {
                    if (failOnStartup) {
                        addError("Unable to parse property string: " + property);
                    } else {
                        addWarn("Unable to parse property string: " + property);
                    }
                }
            });
        }
    }

    // aka unit test friendly
    void startProducer(Properties props) throws Exception {
        producer = new KafkaProducer<>(props);
    }
}
