package de.jeff_media.updatechecker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;

class SpigetMapper implements ThrowingFunction<BufferedReader,String, IOException> {
    @Override
    public String apply(BufferedReader bufferedReader) {
        return new Gson().fromJson(bufferedReader, JsonObject.class).get("name").getAsString();
    }
}
