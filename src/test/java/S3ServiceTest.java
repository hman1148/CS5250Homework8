import org.example.S3Service;
import org.example.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class S3ServiceTest {

    @Mock
    private S3Client s3Client;
    private S3Service s3Service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        s3Service = new S3Service();
    }

    @Test
    public void testReadWidgetRequestsFromBucket_Success() {
        // Mock ListObjectsV2 response
        ListObjectsV2Response listResponse = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("testKey").build())
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResponse);

        // Mock GetObject response
        GetObjectResponse getObjectResponse = GetObjectResponse.builder().build();
        ResponseInputStream<GetObjectResponse> responseStream =
                new ResponseInputStream<>(getObjectResponse, new ByteArrayInputStream("testWidgetData".getBytes()));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

        // Test the readWidgetRequestsFromBucket method
        String result = s3Service.readWidgetRequestsFromBucket("testBucket");
        assertEquals("testWidgetData", result);
    }

    @Test
    public void testStoreWidgetsInS3() {

        Widget widget = new Widget("create", "1234", "1234", "hunter", "test", "test", null, "test");

        // Mock PutObject response
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // Test the storeWidgetsInS3 method
        s3Service.storeWidgetsInS3("testBucket", widget);

        // Verify that putObject was called once with the correct parameters
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }


}