package org.example.API;

import java.util.HashMap;

public class ResponseBuilder {

    public static HashMap<String, Object> buildResponse(int statusCode, String body) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        response.put("body", body);
        return response;
    }
}
