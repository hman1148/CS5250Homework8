package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamoDBService {

    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DynamoDBService() {
        dynamoDbClient = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    public void storeWidgetsInDynamoDB(String tableName, Widget widget) {
        try {
            // Convert widget object to a map
            Map<String, Object> widgetMap = objectMapper.convertValue(widget, Map.class);

            // Create a HashMap to store attrs in DynamoDB
            HashMap<String, AttributeValue> item = new HashMap<>();

            // Add each widget value to a key and value for dynamo
            for (Map.Entry<String, Object> entry : widgetMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value != null) {
                    item.put(key, AttributeValue.builder().s(value.toString()).build());
                }
            }

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);
            System.out.println("Widget: " + widget.getWidgetId() + " stored in DynamoDB");

        } catch (SdkException ex) {
            System.err.println("Failed to store widget: " + widget.getWidgetId() + " into DynamoDB " + ex.getMessage());
        }
    }

    public void updateWidgetInDynamoDB(String tablename, Widget widget) {
        try {
            // Define the primary key of the item to update
            HashMap<String, AttributeValue> key = new HashMap<>();
            key.put("widgetId", AttributeValue.builder().s(widget.getWidgetId()).build());

            UpdateComponents updateComponents = buildUpdateComponents(widget);

            if (updateComponents.getExpressionAttributeValues().isEmpty()) {
                System.err.println("No field to update were provided.");
            }

            Map<String, AttributeValue> attributeValues = updateComponents.getExpressionAttributeValues().entrySet()
                    .stream().collect(Collectors.toMap(Map.Entry::getKey, e -> AttributeValue
                            .builder().s(e.getValue()).build()));

            // build Update request
            UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                    .tableName(tablename)
                    .key(key)
                    .updateExpression(updateComponents.getUpdateExpression())
                    .expressionAttributeNames(updateComponents.getExpressionAttributeNames())
                    .expressionAttributeValues(attributeValues)
                    .build();

            this.dynamoDbClient.updateItem(updateItemRequest);
            System.out.println("Widget updated successfully in DynamoDB: " + widget.getWidgetId());
        } catch (Exception ex) {
            System.err.println("Failed to update widget: " + widget.getWidgetId());
        }
    }

    public void deleteWidgetInDynamoDB(String tablename, Widget widget) {
        try {
            //Define the primary key of the item to remove
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("widgetId", AttributeValue.builder().s(widget.getWidgetId()).build());

            // Make Delete Request
            DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                    .tableName(tablename)
                    .key(key)
                    .build();

            this.dynamoDbClient.deleteItem(deleteItemRequest);
            System.out.println("Widget with ID: " + widget.getWidgetId() + " deleted successfully");

        } catch (Exception ex) {
            System.err.println("Failed to delete widget: " + widget.getWidgetId());
        }
    }

    private UpdateComponents buildUpdateComponents(Widget widget) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> widgetMap = objectMapper.convertValue(widget, Map.class);

        StringBuilder updateExpression = new StringBuilder("SET ");
        Map<String, String> expressionAttributeNames = new HashMap<>();
        Map<String, String> expressionAttributeValues = new HashMap<>();

        widgetMap.forEach((field, value) -> {
            if (value != null && !"widgetId".equals(field)) {
                String attributeName = "#" + field;
                String attributeValue = ":" + field;

                expressionAttributeNames.put(attributeName, field);
                try {
                    expressionAttributeValues.put(attributeValue, value instanceof List
                            ? objectMapper.writeValueAsString(value)
                            : value.toString());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error processing JSON for field: " + field, e);
                }

                updateExpression.append(attributeName).append(" = ").append(attributeValue).append(", ");
            }
        });

        // Remove trailing comma and space if the update expression has content
        if (updateExpression.length() > 4) {
            updateExpression.setLength(updateExpression.length() - 2);
        }

        return new UpdateComponents(updateExpression.toString(), expressionAttributeNames, expressionAttributeValues);
    }
}
