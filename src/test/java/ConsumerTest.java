import org.example.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.exception.SdkException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ConsumerTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private DynamoDBService dynamoDBService;

    @Mock
    private Options options;

    @Mock
    private SimpleQueueService simpleQueueService;

    private Consumer consumerTest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        consumerTest = new Consumer(s3Service, dynamoDBService, simpleQueueService, options);
    }

    @Test
    public void testStartReceiving_S3Storage() throws Exception {
        when(options.getBucket2()).thenReturn("bucket2");
        when(options.getBucket3()).thenReturn("bucket3");
        when(s3Service.readWidgetRequestsFromBucket("bucket2")).thenReturn("{\"widgetId\":\"1234\", \"owner\":\"Sue Smith\"}");

        Thread consumerThread = new Thread(() -> consumerTest.StartingReceving());
        consumerThread.start();
        Thread.sleep(200);
        consumerThread.interrupt();

        verify(s3Service, atLeastOnce()).storeWidgetsInS3(eq("bucket3"), any(Widget.class));
    }

    @Test
    public void testStartReceiving_DynamoDBStorage() throws Exception {
        when(options.getBucket2()).thenReturn("bucket2");
        when(options.getDynamoDBTable()).thenReturn("widgetsTable");
        when(s3Service.readWidgetRequestsFromBucket("bucket2")).thenReturn("{\"widgetId\":\"1234\", \"owner\":\"Sue Smith\"}");

        Thread consumerThread = new Thread(() -> consumerTest.StartingReceving());
        consumerThread.start();
        Thread.sleep(200);
        consumerThread.interrupt();

        verify(dynamoDBService, atLeastOnce()).storeWidgetsInDynamoDB(eq("widgetsTable"), any(Widget.class));
    }

    @Test
    public void testStartReceiving_SdkExceptions() throws Exception {
        when(options.getBucket2()).thenReturn("bucket2");
        when(s3Service.readWidgetRequestsFromBucket("bucket2")).thenThrow(SdkException.builder().message("AWS error").build());

        Thread consumerThread = new Thread(() -> consumerTest.StartingReceving());
        consumerThread.start();
        Thread.sleep(200);
        consumerThread.interrupt();

        verify(s3Service, never()).storeWidgetsInS3(anyString(), any(Widget.class));
        verify(dynamoDBService, never()).storeWidgetsInDynamoDB(anyString(), any(Widget.class));
    }
}