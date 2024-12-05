import org.example.DynamoDBService;
import org.example.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class DynamoDbServiceTest {

    @Mock
    private DynamoDbClient dynamoDbClient;
    @InjectMocks
    private DynamoDBService dynamoDBService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testStorageWidgetsInDynamoDB() {
        Widget widget = new Widget("create", "1234", "1234", "hunter", "test", "test", null, "test");

        when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenReturn(PutItemResponse.builder().build());
        this.dynamoDBService.storeWidgetsInDynamoDB("testDynamo", widget);

        Map<String, AttributeValue> expectedItem = new HashMap<>();
        expectedItem.put("widgetId", AttributeValue.builder().s("1234").build());
        expectedItem.put("type", AttributeValue.builder().s("create").build());
        expectedItem.put("requestId", AttributeValue.builder().s("1234").build());
        expectedItem.put("ownder", AttributeValue.builder().s("hunter").build());
        expectedItem.put("label", AttributeValue.builder().s("test").build());
        expectedItem.put("description", AttributeValue.builder().s("test").build());
        expectedItem.put("field", AttributeValue.builder().s("test").build());

        verify(dynamoDbClient, times(1)).putItem((PutItemRequest) argThat(argument -> {
            PutItemRequest request = (PutItemRequest) argument;
            return "testDynamo".equals(request.tableName()) &&
                    request.item().equals(expectedItem);
        }));
    }
}