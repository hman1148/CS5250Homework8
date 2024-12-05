import org.example.SimpleQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


public class SimpleQueueServiceTest {

    @Mock
    private SqsClient sqsClient;

    @InjectMocks
    private SimpleQueueService simpleQueueService;

    private final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/224193139309/cs5250-requests";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        simpleQueueService = new SimpleQueueService(QUEUE_URL);
    }

    @Test
    public void testGetMessage() {
        Message testMessage = Message.builder()
                .body("This is a test message body")
                .receiptHandle("test-receipt-handle")
                .build();

        ReceiveMessageResponse receiveMessageResponse = ReceiveMessageResponse.builder()
                .messages(testMessage)
                .build();

        when(this.sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResponse);

        Message receivedMessage = simpleQueueService.getMessage();
        assertNotNull(receivedMessage, "Expected a message to be returned");
        assertEquals("This is a test message", receivedMessage.body(), "The message content does not match");

        verify(this.sqsClient, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
    }

    @Test
    public void testDeleteMessage() {
        Message testMessage = Message.builder()
                .body("This is a test message body")
                .receiptHandle("test-receipt-handle")
                .build();

        DeleteMessageResponse deleteMessageResponse = DeleteMessageResponse.builder().build();
        when(this.sqsClient.deleteMessage(any(DeleteMessageRequest.class))).thenReturn(deleteMessageResponse);

        this.simpleQueueService.deleteMessage(testMessage);

        verify(sqsClient, times(1)).deleteMessage(argThat((DeleteMessageRequest request) ->
                request.queueUrl().equals(QUEUE_URL) &&
                        request.receiptHandle().equals(testMessage.receiptHandle())));
    }

    @Test
    public void testWhenQueueShouldBeEmpty() {
        ReceiveMessageResponse receiveMessageResponse = ReceiveMessageResponse.builder()
                .messages(Collections.emptyList())
                .build();

        when(this.sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResponse);

        Message receivedMessage = this.simpleQueueService.getMessage();
        assertNull(receivedMessage, "Expected null when no messages are available");

        verify(this.sqsClient, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
    }

}
