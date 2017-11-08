package xttran.classifier.util;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static String serialize(Object object) {

        if (object == null) {
            return null;
        }

        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T derialize(String json, Class<T> clazz) {

        if (json == null) {
            return null;
        }

        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
} 
