package ru.sberned.kafkalogback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;
import ch.qos.logback.core.Layout;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by empatuk on 30/11/2016.
 */
public class KafkaAppenderTest {
    private KafkaProducer<Long, String> mockerProducer = mock(KafkaProducer.class);
    private KafkaAppender appender;

    private class TestKafkaAppender extends KafkaAppender {
        TestKafkaAppender(String bootstrapservers, String topic, String valueSerialier, Layout<ILoggingEvent> layout) {
            setBootstrapServers(bootstrapservers);
            setTopic(topic);
            setValueSerializer(valueSerialier);
            setLayout(layout);
        }

        @Override
        void startProducer(Properties props) {
            super.producer = mockerProducer;
        }
    }

    private class ExceptionalTestKafkaAppender extends TestKafkaAppender {
        ExceptionalTestKafkaAppender(String bootstrapservers, String topic, String valueSerialier, Layout<ILoggingEvent> layout) {
            super(bootstrapservers, topic, valueSerialier, layout);
        }

        @Override
        void startProducer(Properties props) {
            throw new RuntimeException("oooops");
        }
    }

    @Test(expected = NullPointerException.class)
    public void testLayoutIsMissing() {
        appender = new TestKafkaAppender("servers", "topic", "serializer", null);
        appender.start();
    }

    @Test(expected = NullPointerException.class)
    public void testTopicIsMissing() {
        appender = new TestKafkaAppender("servers", null, "serializer", new JsonLayout());
        appender.start();
    }

    @Test(expected = NullPointerException.class)
    public void testSerializerIsMissing() {
        appender = new TestKafkaAppender("servers", "topic", null, new JsonLayout());
        appender.start();
    }

    @Test(expected = NullPointerException.class)
    public void testBootstrapServersAreMissing() {
        appender = new TestKafkaAppender(null, "topic", "serializer", new JsonLayout());
        appender.start();
    }

    @Test
    public void testStart() {
        appender = new TestKafkaAppender("servers", "topic", "serializer", new JsonLayout());
        appender.start();
    }

    @Test
    public void testParseProperties() {
        final String key1 = "key1", value1 = "value1", key2 = "key2";
        appender = new TestKafkaAppender("servers", "topic", "serializer", new JsonLayout());
        appender.setCustomProps(Arrays.asList(key1 + "|" + value1, key2 + ":value2"));

        Properties properties = new Properties();
        appender.parseProperties(properties);
        assertEquals(value1, properties.getProperty(key1));
        assertNull(properties.getProperty(key2));
    }

    @Test
    public void testStartWithException() {
        appender = new ExceptionalTestKafkaAppender("servers", "topic", "serializer", new JsonLayout());
        appender.start();
    }

    @Test
    public void testLoggingDoesntFailAfterException() {
        appender = new ExceptionalTestKafkaAppender("servers", "topic", "serializer", new JsonLayout());
        appender.start();
        appender.append(new LoggingEvent());
    }

    @Test
    public void testLoggingDoesntFailAfterExceptionInSend() throws ExecutionException, InterruptedException {
        JsonLayout layout = mock(JsonLayout.class);
        appender = new TestKafkaAppender("servers", "topic", "serializer", layout);
        appender.start();
        Future<RecordMetadata> result = mock(Future.class);
        when(result.get()).thenReturn(null);
        when(mockerProducer.send(any(ProducerRecord.class))).thenReturn(result);
        ILoggingEvent event = new LoggingEvent();
        when(layout.doLayout(event)).thenReturn("");
        appender.append(event);
    }
}
