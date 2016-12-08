package ru.sberned.kafkalogback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
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

        assertTrue(result.size() == 0);
    }

    @Test
    public void testEmptyCallerDataDoesntFailLayout() {
        Map<String, Object> result = new HashMap<>();
        layout.addCustomDataToJsonMap(result, new LoggingEvent());

        assertTrue(result.size() == 1);
        assertNotNull(result.get(HOST));
    }

    @Test
    public void testCallerDataAreLogged() {
        Map<String, Object> result = new HashMap<>();
        layout.addCustomDataToJsonMap(result, event());

        assertTrue(result.size() == 5);
        assertNotNull(result.get(HOST));
        assertEquals(result.get(CLASS_NAME), "class");
        assertEquals(result.get(METHOD_NAME), "method");
        assertEquals(result.get(FILE_NAME), "file");
        assertEquals(result.get(LINE_NUMBER), 3);
    }

    @Test
    public void testAdditionalFieldsAreParsedCorrectly() {
        final String key1 = "key1", value1 = "value1", key2 = "key2";
        Map<String, Object> result = new HashMap<>();
        layout.setAdditionalFields(Arrays.asList(key1 + "|" + value1, key2 + ":value2"));
        layout.addCustomDataToJsonMap(result, new LoggingEvent());

        assertTrue(result.size() == 2);
        assertNotNull(result.get(HOST));
        assertEquals(result.get(key1), value1);
        assertNull(result.get(key2));
    }
}
