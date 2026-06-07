package com.example.begger.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads message templates from JSON resource files at runtime.
 * Falls back to empty lists if resources are missing.
 */
public final class MessageLoader {

    private static final Gson GSON = new Gson();
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();
    private static final String BASE_PATH = "/assets/rankbegger/messages/";

    private MessageLoader() {}

    public static List<String> loadBegMessages() {
        return load("beg_messages.json");
    }

    public static List<String> loadGiftResponses() {
        return load("gift_responses.json");
    }

    public static List<String> loadMvpPlusPlusMessages() {
        return load("mvppp_messages.json");
    }

    public static List<String> loadIdMessages() {
        return load("id_messages.json");
    }

    private static List<String> load(String filename) {
        try {
            InputStream is = MessageLoader.class.getResourceAsStream(BASE_PATH + filename);
            if (is == null) {
                System.err.println("[Begger] Missing resource: " + BASE_PATH + filename);
                return new ArrayList<>();
            }
            List<String> result = GSON.fromJson(new InputStreamReader(is, "UTF-8"), STRING_LIST_TYPE);
            is.close();
            return result != null ? new ArrayList<>(result) : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
