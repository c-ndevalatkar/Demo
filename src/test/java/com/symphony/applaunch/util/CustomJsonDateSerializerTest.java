package com.symphony.applaunch.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CustomJsonDateSerializerTest {

    private final CustomJsonDateSerializer serializer = new CustomJsonDateSerializer();

    @Test
    void serialize_validDate_writesFormattedString() throws Exception {
        // Arrange: build a Date for a known day using default timezone
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2025);
        cal.set(Calendar.MONTH, Calendar.JANUARY); // 0-based
        cal.set(Calendar.DAY_OF_MONTH, 17);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();

        StringWriter writer = new StringWriter();
        JsonFactory factory = new JsonFactory();
        JsonGenerator generator = factory.createGenerator(writer);

        // Act
        serializer.serialize(date, generator, null);
        generator.flush(); // make sure everything is written

        // Assert
        // CustomJsonDateSerializer writes a JSON string value, so we expect quotes in the output
        assertEquals("\"01/17/2025\"", writer.toString());
    }

    @Test
    void serialize_nullDate_handlesExceptionGracefully() throws Exception {
        StringWriter writer = new StringWriter();
        JsonFactory factory = new JsonFactory();
        JsonGenerator generator = factory.createGenerator(writer);

        // Act & Assert: method should not throw even though it will hit the catch block
        assertDoesNotThrow(() -> serializer.serialize(null, generator, null));

        generator.flush();

        // Since the exception path logs but doesn't write anything, output should be empty
        assertEquals("", writer.toString());
    }
}

