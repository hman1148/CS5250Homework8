# Use the latest Alpine Linux with OpenJDK 8 JRE
FROM openjdk:8-jre-alpine

LABEL authors="Hunter Peart"

# Set the working directory inside the container
WORKDIR /app

# Copy the application JAR file into the container
COPY build/libs/Homework7-1.0-all.jar Homework7-1.0-all.jar

# Define the default command to run the application
CMD ["java", "-jar", "Homework7-1.0-all.jar", "--bucket2", "usu-cs5250-perat2-dist", "--bucket3", "usu-cs5250-perat3-dist", "--sqs", "https://sqs.us-east-1.amazonaws.com/224193139309/cs5250-requests", "--dynamodb-table", "widgets"]
