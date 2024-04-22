package ru.sberned.kafkalogback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.sberned.kafkalogback.CustomJsonLayout.*;

/**
 * Created by empatuk on 08/12/2016.
 */
public class CustomJsonLayoutTest {
    CustomJsonLayout layout = new CustomJsonLayout();

    private ILoggingEvent event() {
        LoggingEvent event = new LoggingEvent();
        event.setCallerData(new StackTraceElement[]{new StackTraceElement("class", "method", "file", 3)});
        return event;
    }

    @Test
    public void testDisabledArgumetsAreNotAdded() {
        layout.setIncludeClassName(false);
        layout.setIncludeHost(false);
        layout.setIncludeLineNumber(false);
        layout.setIncludeMethodName(false);
        layout.setIncludeFileName(false);
        Map<String, Object> result = new HashMap<>();
        layout.addCustomDataToJsonMap(result, event());

        assertEquals(0, result.size());
    }

    @Test
    public void testEmptyCallerDataDoesntFailLayout() {
        Map<String, Object> result = new HashMap<>();
        layout.addCustomDataToJsonMap(result, new LoggingEvent());

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(HOST)).isNotNull();
    }

    @Test
    public void testCallerDataAreLogged() {
        Map<String, Object> result = new HashMap<>();
        layout.addCustomDataToJsonMap(result, event());

        assertThat(result.size()).isEqualTo(5);
        assertThat(result.get(HOST)).isNotNull();
        assertThat(result.get(CLASS_NAME)).isEqualTo("class");
        assertThat(result.get(METHOD_NAME)).isEqualTo("method");
        assertThat(result.get(FILE_NAME)).isEqualTo("file");
        assertThat(result.get(LINE_NUMBER)).isEqualTo(3);
    }

    @Test
    public void testAdditionalFieldsAreParsedCorrectly() {
        final String key1 = "key1", value1 = "value1", key2 = "key2";
        Map<String, Object> result = new HashMap<>();
        layout.setAdditionalFields(Arrays.asList(key1 + "|" + value1, key2 + ":value2"));
        layout.addCustomDataToJsonMap(result, new LoggingEvent());

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(HOST)).isNotNull();
        assertThat(result.get(key1)).isEqualTo(value1);
        assertThat(result.get(key2)).isNull();
    }
}
