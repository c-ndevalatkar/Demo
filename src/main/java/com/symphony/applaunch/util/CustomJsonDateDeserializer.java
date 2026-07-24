package com.symphony.applaunch.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.symphony.applaunch.constants.ApplicationConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class CustomJsonDateDeserializer extends JsonDeserializer<Date> {
    private static final Logger logger = LoggerFactory.getLogger(CustomJsonDateDeserializer.class);

    @Override
    public Date deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext) {

        SimpleDateFormat format = new SimpleDateFormat(DateHelper.US_DATE_FORMAT);
        String date;
        try {
            date = jsonparser.getText();
            return format.parse(date);
        } catch (ParseException e) {
            logger.info(ApplicationConstants.CATCH_MESSAGE + e);
            try {
                date = jsonparser.getText();
                long milliSeconds = Long.parseLong(date);
                return new Date(milliSeconds);
            } catch (IOException e1) {
                log.error(e1.getMessage());
            }
            return null;
        } catch (IOException e1) {
            logger.info(ApplicationConstants.CATCH_MESSAGE + e1);
            return null;
        }
    }

}
