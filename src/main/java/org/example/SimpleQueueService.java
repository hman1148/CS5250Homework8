package org.example;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.LinkedList;
import java.util.List;

public class SimpleQueueService {

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final LinkedList<Message> messageCache = new LinkedList<Message>();

    public SimpleQueueService(String queueUrl) {
        this.sqsClient = SqsClient.builder().build();
        this.queueUrl = queueUrl;
    }

    public Message getMessage() {
        if (messageCache.isEmpty()) {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(this.getQueueUrl())
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(10)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
            messageCache.addAll(messages);
        }
        return messageCache.poll();
    }

    public void deleteMessage(Message message) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest
                .builder()
                .queueUrl(this.getQueueUrl())
                .receiptHandle(message.receiptHandle())
                .build();

        this.sqsClient.deleteMessage(deleteMessageRequest);
    }

    public void sendMessage(String message) {
        try {
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(this.getQueueUrl())
                    .messageBody(message)
                    .build();
            this.sqsClient.sendMessage(sendMessageRequest);
            System.out.println("Message sent to SQS queue: " + message);
        } catch (Exception e) {
            System.err.println("Failed to send message to SQS: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public String getQueueUrl() {
        return queueUrl;
    }
}
