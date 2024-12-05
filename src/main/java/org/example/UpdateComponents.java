package org.example;

import java.util.Map;

public class UpdateComponents {

    private final String updateExpression;
    private final Map<String, String> expressionAttributeNames;
    private final Map<String, String> expressionAttributeValues;

    public UpdateComponents(String updateExpression, Map<String, String> expressionAttributeNames,
                            Map<String, String> expressionAttributeValues) {
        this.updateExpression = updateExpression;
        this.expressionAttributeNames = expressionAttributeNames;
        this.expressionAttributeValues = expressionAttributeValues;
    }

    public String getUpdateExpression() {
        return updateExpression;
    }

    public Map<String, String> getExpressionAttributeNames() {
        return expressionAttributeNames;
    }

    public Map<String, String> getExpressionAttributeValues() {
        return expressionAttributeValues;
    }
}
