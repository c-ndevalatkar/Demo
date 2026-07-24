package com.symphony.applaunch.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.symphony.applaunch.constants.ApplicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomJsonDateSerializer extends JsonSerializer<Date> {
    private static final Logger logger = LoggerFactory.getLogger(CustomJsonDateSerializer.class);

    @Override
    public void serialize(Date arg0, JsonGenerator arg1, SerializerProvider arg2) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            String format = formatter.format(arg0);
            arg1.writeString(format);// Tue Jan 17 2017 00:00:00 GMT+0530 (India
            // Standard Time)
        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE + e);
        }
    }
}
