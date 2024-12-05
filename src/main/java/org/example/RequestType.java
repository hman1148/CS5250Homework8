
package org.example;

public enum RequestType {
    CREATE, UPDATE, DELETE, UNKNOWN;

    public static RequestType fromString(String request) {
        switch (request.toLowerCase()) {
            case "create":
                return CREATE;
            case "update":
                return UPDATE;
            case "delete":
                return DELETE;
            default:
                return UNKNOWN;
        }
    }
}
