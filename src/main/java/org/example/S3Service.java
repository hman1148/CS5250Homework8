package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class S3Service {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public S3Service() {
        s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    public String readWidgetRequestsFromBucket(String bucket) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .maxKeys(1)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            if (!listResponse.contents().isEmpty()) {
                S3Object object = listResponse.contents().get(0);

                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(object.key())
                        .build();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        s3Client.getObject(getObjectRequest)))) {

                    StringBuilder widgetRequest = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        widgetRequest.append(line);
                    }

                    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(object.key())
                            .build();
                    s3Client.deleteObject(deleteObjectRequest);

                    return widgetRequest.toString();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (SdkException ex) {
            System.err.println("Failed to read from bucket: " + bucket + " - " + ex.getMessage());
        }
        return null;
    }

    public void storeWidgetsInS3(String bucket, Widget widget) {
        try {
            String ownerKey = widget.getOwner().replace(" ", "-").toLowerCase();
            String objectKey = String.format("widgets/%s/%s", ownerKey, widget.getWidgetId());

            String widgetJson = this.objectMapper.writeValueAsString(widget);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromString(widgetJson));

            System.out.println("Widget " + widget.getWidgetId() + " stored in bucket " + bucket);
        } catch (Exception ex) {
            System.err.println("Failed to store widget " + widget.getWidgetId() + " in s3 " + ex.getMessage());
        }
    }

    public void updateWidgetInS3(String bucket, Widget widget) {
        try {
            String widgetJson = this.objectMapper.writeValueAsString(widget);

            String objectKey = "widgets/" + widget.getWidgetId() + ".json";

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            this.s3Client.putObject(request, RequestBody.fromString(widgetJson));
            System.out.println("Successfully updated widget in S3: " + widget.getWidgetId());
        } catch (Exception e) {
            System.err.println("Error for update request: " + e.getMessage());
        }
    }

    public void deleteWidgetInS3(String bucket, Widget widget) {
        try {
            String widgetJson = this.objectMapper.writeValueAsString(widget);

            String objectKey = "widgets/" + widget.getWidgetId() + ".json";

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            this.s3Client.deleteObject(request);
            System.out.println("Successfully deleted widget in S3: " + widget.getWidgetId());

        } catch (Exception ex) {
            System.err.println("Error occured for delete request: " + ex.getMessage());
        }
    }

}
