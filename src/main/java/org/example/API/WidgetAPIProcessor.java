package org.example.API;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DynamoDBService;
import org.example.SimpleQueueService;
import org.example.Widget;

public class WidgetAPIProcessor {

    private final DynamoDBService dynamoDBService;
    private final SimpleQueueService simpleQueueService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String TABLE_NAME = "widgets";

    public WidgetAPIProcessor(DynamoDBService dynamoDBService, SimpleQueueService simpleQueueService) {
        this.dynamoDBService = dynamoDBService;
        this.simpleQueueService = simpleQueueService;
    }

    public void createWidget(Widget widget, Context context) {
        if (this.dynamoDBService != null) {
            this.dynamoDBService.storeWidgetsInDynamoDB(this.TABLE_NAME, widget);
            context.getLogger().log("Widget stored in DynamoDB: " + widget.getWidgetId());
        }

        if (this.simpleQueueService != null) {
            try {
                String widgetJson = this.objectMapper.writeValueAsString(widget);
                this.simpleQueueService.sendMessage(widgetJson);
                context.getLogger().log("Widget added to SQS: " + widget.getWidgetId());
            } catch (Exception e) {
                context.getLogger().log("Failed to publish widget to SQS: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public void updateWidget(Widget widget, Context context) {
        if (this.dynamoDBService != null) {
            this.dynamoDBService.updateWidgetInDynamoDB(this.TABLE_NAME, widget);
            context.getLogger().log("Widget updated in DynamoDB: " + widget.getWidgetId());
        }
    }

    public void deleteWidget(Widget widget, Context context) {
        if (this.dynamoDBService != null) {
            this.dynamoDBService.deleteWidgetInDynamoDB(this.TABLE_NAME, widget);
            context.getLogger().log("Widget deleted from DynamoDB: " + widget.getWidgetId());
        }
    }

}
