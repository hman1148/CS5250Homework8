package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class WidgetLambdaController implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String TABLE_NAME = "widgets";
    private final DynamoDBService dynamoDBService;
    private final SimpleQueueService simpleQueueService;

    public WidgetLambdaController(DynamoDBService dynamoDBService, SimpleQueueService simpleQueueService) {
        this.dynamoDBService = dynamoDBService;
        this.simpleQueueService = simpleQueueService;
    }


    @Override
    public HashMap<String, Object> handleRequest(Map<String, Object> stringObjectMap, Context context) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            // Parse the request body
            String body = (String) stringObjectMap.get("body");

            if (body == null || body.isEmpty()) {
                response.put("statusCode", 400);
                response.put("body", "Invalid request: Missing request body");
                return response;
            }
            Widget widget = this.objectMapper.readValue(body, Widget.class);

            // Validate object mapper widget
            if (!this.isValidRequest(widget)) {
                response.put("statusCode", 400);
                response.put("body", "Invalid Widget Request: Missing required fields");
                return response;
            }
            // process the widget
            this.processWidget(widget);

            // Return success response
            response.put("statusCode", 200);
            response.put("body", "Widget successfully processed: " + widget.getWidgetId());
        } catch (Exception e) {
            context.getLogger().log("Error processing widget: " + e.getMessage());
            response.put("statusCode", 500);
            response.put("body", "Internal Server Error, Widget Failed to be processed");
        }
        return response;
    }

    private boolean isValidRequest(Widget widget) {
        // Validate required fields
        return widget.getWidgetId() != null && !widget.getWidgetId().isEmpty()
                && widget.getType() != null && !widget.getType().isEmpty()
                && widget.getRequestId() != null && !widget.getRequestId().isEmpty()
                && widget.getOwner() != null && !widget.getOwner().isEmpty();
    }

    private void processWidget(Widget widget) {
        // Add widget to Queue and DynamoDB
        if (widget == null) {
            return;
        }

        if (this.dynamoDBService != null) {
            try {
                this.dynamoDBService.storeWidgetsInDynamoDB(this.TABLE_NAME, widget);
                System.out.println("Successfully Stored Widget In DynamoDB: " + widget.getWidgetId());
            } catch (Exception e) {
                System.err.println("Failed to store widget in DynamoDB: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        if (this.simpleQueueService != null) {
            try {
                String widgetJson = this.objectMapper.writeValueAsString(widget);
                this.simpleQueueService.sendMessage(widgetJson);
                System.out.println("Successfully Added Widget to SQS Queue: " + widget.getWidgetId());
            } catch (Exception e) {
                System.err.println("Failed to add Widget to SQS Queue: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
