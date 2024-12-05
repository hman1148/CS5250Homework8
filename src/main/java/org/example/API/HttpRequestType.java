package org.example.API;

public enum HttpRequestType {
    POST, PUT, DELETE, UNKNOWN;

    public static HttpRequestType fromString(String request) {
        switch (request.toLowerCase()) {
            case "post":
                return POST;
            case "put":
                return PUT;
            case "delete":
                return DELETE;
            default:
                return UNKNOWN;
        }
    }
}
