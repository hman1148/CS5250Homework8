
package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.model.Message;


public class Consumer {

    private final S3Service s3Service;
    private final DynamoDBService dynamoDBService;
    private final Options options;
    private final SimpleQueueService simpleQueueService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Consumer(S3Service s3Service, DynamoDBService dynamoDBService, SimpleQueueService simpleQueueService, Options options) {
        this.s3Service = s3Service;
        this.dynamoDBService = dynamoDBService;
        this.options = options;
        this.simpleQueueService = simpleQueueService;
    }

    public void StartingReceving() {
        while (true) {
            try {
                // Read the widget requests
                String widgetRequestJson;
                if (this.options.isUseSqs()) {
                    Message sqsMessage = this.simpleQueueService.getMessage();

                    if (sqsMessage != null) {
                        widgetRequestJson = this.simpleQueueService.getMessage().body();
                        processWidgetRequest(widgetRequestJson);
                        this.simpleQueueService.deleteMessage(sqsMessage);
                    } else {
                        Thread.sleep(1000);
                    }

                } else {
                    widgetRequestJson = this.s3Service.readWidgetRequestsFromBucket(options.getBucket2());

                    if (widgetRequestJson != null) {
                        processWidgetRequest(widgetRequestJson);
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

        }
    }

    private void processWidgetRequest(String widgetRequestJson) {
        try {
            Widget widget = objectMapper.readValue(widgetRequestJson, Widget.class);
            RequestType requestType = RequestType.fromString(widget.getType());

            switch (requestType) {
                case CREATE:
                    processCreateRequest(widget);
                    break;
                case UPDATE:
                    proccessUpdateRequest(widget);
                    break;
                case DELETE:
                    processDeleteRequest(widget);
                    break;
                default:
                    System.err.println("Unknown request type");
                    break;
            }
        } catch (Exception ex) {
            System.err.println("Failed to process widget request: " + ex.getMessage());
        }
    }

    private void processCreateRequest(Widget widget) {
        try {
            if (this.options.getBucket3() != null) {
                s3Service.storeWidgetsInS3(this.options.getBucket3(), widget);
            } else if (this.options.getDynamoDBTable() != null) {
                dynamoDBService.storeWidgetsInDynamoDB(this.options.getDynamoDBTable(), widget);
            }
        } catch (Exception ex) {
            System.err.println("Failed to process create request: " + widget.getWidgetId() + " Error: " + ex.getMessage());
        }
    }

    private void proccessUpdateRequest(Widget widget) {
        try {
            if (this.options.getBucket3() != null) {
                this.s3Service.updateWidgetInS3(this.options.getBucket3(), widget);
            } else if (this.options.getDynamoDBTable() != null) {
                this.dynamoDBService.updateWidgetInDynamoDB(this.options.getDynamoDBTable(), widget);
            }
        } catch (Exception ex) {
            System.err.println("Failed to process update request: " + widget.getWidgetId() + " Error: " + ex.getMessage());
        }
    }

    private void processDeleteRequest(Widget widget) {
        try {
            if (this.options.getBucket3() != null) {
                this.s3Service.deleteWidgetInS3(this.options.getBucket3(), widget);
            } else if (this.options.getDynamoDBTable() != null) {
                this.dynamoDBService.deleteWidgetInDynamoDB(this.options.getDynamoDBTable(), widget);
            }
        } catch (Exception ex) {
            System.err.println("Failed to process delete request: " + widget.getWidgetId() + " Error " + ex.getMessage());
        }
    }
}
