/*
 * Copyright (c) 2022 Alexander Majka (mfnalex), JEFF Media GbR
 * Website: https://www.jeff-media.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jeff_media.updatechecker;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;

interface VersionMapper {
    ThrowingFunction<BufferedReader,String,IOException> TRIM_FIRST_LINE = reader -> reader.readLine().trim();

    ThrowingFunction<BufferedReader,String,IOException> SPIGET = reader -> new Gson().fromJson(reader, JsonObject.class).get("name").getAsString();

    ThrowingFunction<BufferedReader,String,IOException> GITHUB_RELEASE_TAG = reader -> {
        JsonArray array = new Gson().fromJson(reader, JsonArray.class);
        if(array.size()==0) {
            throw new IOException("Could not check for updates: no GitHub release found.");
        }
        JsonObject release = array.get(0).getAsJsonObject();
        return release.get("tag_name").getAsString();
    };
}
