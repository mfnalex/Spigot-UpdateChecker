package de.jeff_media.updatechecker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;

interface VersionMapper {
    ThrowingFunction<BufferedReader,String,IOException> TRIM_FIRST_LINE = reader -> reader.readLine().trim();
    ThrowingFunction<BufferedReader,String,IOException> SPIGET = reader -> new Gson().fromJson(reader, JsonObject.class).get("name").getAsString();
}
