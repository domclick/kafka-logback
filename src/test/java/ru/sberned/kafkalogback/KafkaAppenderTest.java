package ru.sberned.kafkalogback;

import ch.qos.logback.contrib.json.classic.JsonLayout;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Created by empatuk on 30/11/2016.
 */
public class KafkaAppenderTest {
    private KafkaProducer<Long, String> mockerProducer = mock(KafkaProducer.class);
    private KafkaAppender appender;

    private class TestKafkaAppender extends KafkaAppender {
        @Override
        void startProducer(Properties props) {
            super.producer = mockerProducer;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testLayoutIsMissing() {
        appender = new TestKafkaAppender();
        appender.setBootstrapServers("servers");
        appender.setTopic("topic");
        appender.setValueSerializer("serializer");
        appender.start();
    }

    @Test(expected = NullPointerException.class)
    public void testTopicIsMissing() {
        appender = new TestKafkaAppender();
        appender.setBootstrapServers("servers");
        appender.setLayout(new JsonLayout());
        appender.setValueSerializer("serializer");
        appender.start();
    }

    @Test(expected = NullPointerException.class)
    public void testSerializerIsMissing() {
        appender = new TestKafkaAppender();
        appender.setBootstrapServers("servers");
        appender.setLayout(new JsonLayout());
        appender.setTopic("topic");
        appender.start();
    }

    @Test(expected = NullPointerException.class)
    public void testBootstrapServersAreMissing() {
        appender = new TestKafkaAppender();
        appender.setValueSerializer("serializer");
        appender.setLayout(new JsonLayout());
        appender.setTopic("topic");
        appender.start();
    }

    @Test
    public void testStart() {
        appender = new TestKafkaAppender();
        appender.setValueSerializer("serializer");
        appender.setLayout(new JsonLayout());
        appender.setTopic("topic");
        appender.setBootstrapServers("bootstrap");
        appender.start();
    }

    @Test
    public void testParseProperties() {
        final String key1 = "key1", value1 = "value1", key2 = "key2", value2 = "value2";
        appender = new TestKafkaAppender();
        appender.setCustomProps(Arrays.asList("key1=value1", "key2:value2"));

        Properties properties = new Properties();
        appender.parseProperties(properties);
        assertEquals(value1, properties.getProperty(key1));
        assertNull(properties.getProperty(key2));
    }
}
