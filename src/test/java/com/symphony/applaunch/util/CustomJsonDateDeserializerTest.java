package com.symphony.applaunch.util;

import com.fasterxml.jackson.core.JsonParser;
import com.symphony.applaunch.constants.ApplicationConstants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomJsonDateDeserializerTest {

    private final CustomJsonDateDeserializer deserializer = new CustomJsonDateDeserializer();

    /**
     * 1) Happy path: valid date string in US_DATE_FORMAT -> parsed directly.
     */
    @Test
    void deserialize_validDateString_returnsParsedDate() throws Exception {
        // Arrange
        JsonParser parser = mock(JsonParser.class);

        // Adjust the pattern / string to match DateHelper.US_DATE_FORMAT in your code
        String dateString = "03/25/2024"; // assumes MM/dd/yyyy
        when(parser.getText()).thenReturn(dateString);

        SimpleDateFormat format = new SimpleDateFormat(DateHelper.US_DATE_FORMAT);
        Date expected = format.parse(dateString);

        // Act
        Date result = deserializer.deserialize(parser, null);

        // Assert
        assertNotNull(result);
        assertEquals(expected, result);
    }

    /**
     * 2) First parse fails (ParseException) but second attempt treats the value as millis since epoch
     *    and returns a Date from that.
     */
    @Test
    void deserialize_invalidDate_thenMillis_returnsDateFromMillis() throws Exception {
        JsonParser parser = mock(JsonParser.class);

        // First getText() returns a non-parseable date to trigger ParseException
        // Second getText() returns a numeric string that Long.parseLong can parse.
        String invalidDate = "not-a-date";
        long millis = 1711334400000L; // some fixed timestamp
        when(parser.getText()).thenReturn(invalidDate, String.valueOf(millis));

        // Act
        Date result = deserializer.deserialize(parser, null);

        // Assert
        assertNotNull(result);
        assertEquals(new Date(millis), result);
    }

    /**
     * 3) First parse fails with ParseException, second getText() throws IOException,
     *    so we hit the inner catch(IOException) and return null.
     */
    @Test
    void deserialize_parseException_thenIOException_returnsNull() throws Exception {
        JsonParser parser = mock(JsonParser.class);

        // First call: invalid date string -> SimpleDateFormat.parse(...) throws ParseException
        // Second call: IOException -> triggers inner catch(IOException e1) and returns null
        when(parser.getText())
                .thenReturn("invalid-date")               // for initial parse
                .thenThrow(new IOException("IO during millis parsing"));

        // Act
        Date result = deserializer.deserialize(parser, null);

        // Assert
        assertNull(result);
    }

    /**
     * 4) First getText() itself throws IOException -> hits the outer catch(IOException) and returns null.
     */
    @Test
    void deserialize_firstGetTextThrowsIOException_returnsNull() throws Exception {
        JsonParser parser = mock(JsonParser.class);

        // First getText() throws IOException -> outer catch(IOException) path
        when(parser.getText()).thenThrow(new IOException("IO during first getText"));

        // Act
        Date result = deserializer.deserialize(parser, null);

        // Assert
        assertNull(result);
    }
}

