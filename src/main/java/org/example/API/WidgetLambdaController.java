package org.example.API;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DynamoDBService;
import org.example.SimpleQueueService;
import org.example.Widget;

import java.util.HashMap;
import java.util.Map;

public class WidgetLambdaController implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final WidgetAPIProcessor widgetAPIProcessor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/224193139309/cs5250-requests";

    public WidgetLambdaController() {
        DynamoDBService dynamoDBService = new DynamoDBService();
        SimpleQueueService simpleQueueService = new SimpleQueueService(this.QUEUE_URL);
        this.widgetAPIProcessor = new WidgetAPIProcessor(dynamoDBService, simpleQueueService);
    }

    public WidgetLambdaController(DynamoDBService dynamoDBService, SimpleQueueService simpleQueueService) {
        this.widgetAPIProcessor = new WidgetAPIProcessor(dynamoDBService, simpleQueueService);
    }

    @Override
    public HashMap<String, Object> handleRequest(Map<String, Object> stringObjectMap, Context context) {
        try {
            // Get HTTP Method
            String httpMethod = (String) stringObjectMap.get("httpMethod");
            if (httpMethod == null) {
                return ResponseBuilder.buildResponse(400, "Missing HTTP method");

            }

            HttpRequestType httpRequestType = HttpRequestType.fromString(httpMethod);
            switch (httpRequestType) {
                case POST:
                    return this.handleCreate(stringObjectMap, context);
                case PUT:
                    return this.handleUpdate(stringObjectMap, context);
                case DELETE:
                    return this.handleDelete(stringObjectMap, context);
                case UNKNOWN:
                default:
                    return ResponseBuilder.buildResponse(405, "Method Not Allowed");

            }
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return ResponseBuilder.buildResponse(500, "Internal Server Error");
        }
    }

    private boolean isValidRequest(Widget widget) {
        // Validate required fields
        return widget.getWidgetId() != null && !widget.getWidgetId().isEmpty()
                && widget.getType() != null && !widget.getType().isEmpty()
                && widget.getRequestId() != null && !widget.getRequestId().isEmpty()
                && widget.getOwner() != null && !widget.getOwner().isEmpty();
    }

    private HashMap<String, Object> handleCreate(Map<String, Object> input, Context context) {
        try {
            String body = (String) input.get("body");
            Widget widget = this.objectMapper.readValue(body, Widget.class);
            context.getLogger().log("ENV: " + System.getenv("SQS_QUEUE_URL"));

            if (!this.isValidRequest(widget)) {
                return ResponseBuilder.buildResponse(400, "Invalid Widget");
            }

            this.widgetAPIProcessor.createWidget(widget, context);
            return ResponseBuilder.buildResponse(201, "Widget created: " + widget.getWidgetId());

        } catch (Exception e) {
            context.getLogger().log("Error creating widget: " + e.getMessage());
            return ResponseBuilder.buildResponse(500, "Error creating widget: " + e.getMessage());
        }
    }

    private HashMap<String, Object> handleUpdate(Map<String, Object> input, Context context) {
        try {
            String body = (String) input.get("body");
            Widget widget = this.objectMapper.readValue(body, Widget.class);

            if (!this.isValidRequest(widget)) {
                return ResponseBuilder.buildResponse(400, "Invalid Widget");
            }

            this.widgetAPIProcessor.updateWidget(widget, context);
            return ResponseBuilder.buildResponse(200, "Widget updated: " + widget.getWidgetId());
        } catch (Exception e) {
            context.getLogger().log("Error updating widget: " + e.getMessage());
            return ResponseBuilder.buildResponse(500, "Error updating widget: " + e.getMessage());
        }
    }

        private HashMap<String, Object> handleDelete(Map<String, Object> input, Context context) {
            try {
                String body = (String) input.get("body");
                Widget widget = this.objectMapper.readValue(body, Widget.class);

                if (!this.isValidRequest(widget)) {
                    return ResponseBuilder.buildResponse(400, "Invalid Widget");
                }

                this.widgetAPIProcessor.deleteWidget(widget, context);
                return ResponseBuilder.buildResponse(200, "Widget deleted: " + widget.getWidgetId());
            } catch (Exception e) {
                context.getLogger().log("Error deleting widget: " + e.getMessage());
                return ResponseBuilder.buildResponse(500, "Error deleting widget: " + e.getMessage());
            }
        }
}
