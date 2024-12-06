package ApiTest;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.API.WidgetLambdaController;
import org.example.DynamoDBService;
import org.example.SimpleQueueService;
import org.example.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LambdaControllerTest {

    @Mock
    private DynamoDBService dynamoDBService;
    @Mock
    private SimpleQueueService simpleQueueService;
    @Mock
    private Context context;
    @InjectMocks
    private WidgetLambdaController widgetLambdaController;

    @Mock
    private LambdaLogger mockLogger;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(this.context.getLogger()).thenReturn(this.mockLogger);

        doNothing().when(this.mockLogger).log(anyString());
    }

    @Test
    public void testHandleCreateValidRequest() throws  Exception {
        Widget widget = new Widget(
                "widget-type-example",
                "12345",
                "widget123",
                "owner-example",
                "Example Widget",
                "This is an example widget",
                null
        );

        String widgetJson = this.objectMapper.writeValueAsString(widget);

        // Create a mock input
        Map<String, Object> input = new HashMap<>();
        input.put("httpMethod", "POST");
        input.put("body", widgetJson);

        // CAll the handler
        Map<String, Object> response = this.widgetLambdaController.handleRequest(input, this.context);

        assertEquals(201, response.get("statusCode"));
        assertEquals("Widget created: widget123", response.get("body"));
    }

    @Test
    public void testHandleCreateInvalidRequest() {
        String widgetJson = "{ \"type\": \"\", \"requestId\": \"\", \"widgetId\": \"\" }";

        Map<String, Object> input = new HashMap<>();
        input.put("httpMethod", "POST");
        input.put("body", widgetJson);

        Map<String, Object> response = this.widgetLambdaController.handleRequest(input, this.context);
        assertEquals(500, response.get("statusCode"));
        assertEquals("Invalid Widget", response.get("body"));

        verifyNoInteractions(this.dynamoDBService);
        verifyNoInteractions(this.simpleQueueService);
    }

    @Test
    public void testContextLogging() {
        this.context.getLogger().log("Testing logger");
        verify(this.mockLogger, times(1)).log("Testing logger");
    }

}
