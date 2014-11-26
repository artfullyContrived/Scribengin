package com.neverwinterdp.scribengin.util;

import java.util.Map;

import com.beust.jcommander.internal.Maps;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Charsets;

public class Utils {

  /**
   * Deserializer from JSON string in a byte array to a map.
   */
  private static ObjectReader reader;
  static ObjectMapper mapper = new ObjectMapper();

  /**
   * Convert a byte array containing a JSON string to a map of key/value
   * pairs.
   * 
   * @param data
   *            byte array containing the key/value pair string
   * 
   * @return a new map containing the key/value pairs
   */
  public static Map<String, String> toMap(byte[] data) {

    reader = mapper.reader(Map.class);

    if (data == null || data.length == 0) {
      return Maps.newHashMap();
    }
    try {
      return reader.readValue(data);
    } catch (Exception e) {
      String contents;
      contents = new String(data, Charsets.UTF_8);
      throw new RuntimeException(
          "Error parsing JSON string: " + contents, e);
    }
  }

  public static <T> T toClass(byte[] data, Class<T> clazz) {

    reader = mapper.reader(clazz);
    if (data == null || data.length == 0) {
      return null;
    }
    try {
      return reader.readValue(data);
    } catch (Exception e) {
      String contents;
      contents = new String(data, Charsets.UTF_8);
      throw new RuntimeException(
          "Error parsing JSON string: " + contents, e);
    }
  }

  public static <T> String toJson(T data) {

    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    String json = "";
    try {
      json = ow.writeValueAsString(data);
    } catch (JsonProcessingException e) {
    }
    return json;
  }
}
