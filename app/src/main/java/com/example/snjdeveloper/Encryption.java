package com.example.snjdeveloper;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class Encryption {
    public String getEncodedString(String text) {
        return Base64.encodeToString(text.getBytes(), Base64.DEFAULT);
    }

    public String getDecodedString(String text) {
        byte[] data = Base64.decode(text, Base64.DEFAULT);
        return new String(data, StandardCharsets.UTF_8);
    }
}
