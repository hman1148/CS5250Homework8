
package org.example;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Options", mixinStandardHelpOptions = true, description = "Options for the application")
public class Options implements Runnable {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Options.class);

    @Option(names = {"-b", "--bucket3"}, description = "Name of the S3 Bucket to store widgets (Bucket 3)", required = false)
    private String Bucket3;

    @Option(names = {"-d", "--dynamodb-table"}, description = "Name of the DynamoDB table to store widgets", required = false)
    private String DynamoDBTable;

    @Option(names = {"-r", "--bucket2"}, description = "Name of the S3 bucket to read widget requests from (Bucket 2)", required = true)
    private String Bucket2;

    @Option(names = {"--sqs"}, description = "Name of the Simple Queue Service", required = false)
    private String Sqs;

    public String getBucket3() {
        return Bucket3;
    }

    public void setBucket3(String bucket3) {
        Bucket3 = bucket3;
    }

    public String getDynamoDBTable() {
        return DynamoDBTable;
    }

    public void setDynamoDBTable(String dynamoDBTable) {
        DynamoDBTable = dynamoDBTable;
    }

    public String getBucket2() {
        return Bucket2;
    }

    public void setBucket2(String bucket2) {
        Bucket2 = bucket2;
    }

    public String getSqs() {
        return this.Sqs;
    }

    public void setSqs(String sqs) {
        this.Sqs = sqs;
    }

    public boolean isUseSqs() {
        return this.Sqs != null && !this.Sqs.isEmpty();
    }

    @Override
    public void run() {
        // Log the provided options

        ConfigureLogging();

        logger.info("Starting application with the following options:");
        logger.info("Bucket 3: {}", Bucket3);
        logger.info("DynamoDB Table: {}", DynamoDBTable);
        logger.info("Bucket 2 (required): {}", Bucket2);
        logger.info("Sqs: {}", Sqs);

        try {
            S3Service s3Service = new S3Service();
            DynamoDBService dynamoDBService = new DynamoDBService();
            SimpleQueueService simpleQueueService = isUseSqs() ? new SimpleQueueService(this.Sqs) : null;

            Consumer consumer = new Consumer(s3Service, dynamoDBService, simpleQueueService, this);
            consumer.StartingReceving();

            logger.info("Services started successfully.");
        } catch (Exception e) {
            logger.error("An error occurred while running the services: ", e);
        }
    }

    private static void ConfigureLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        //Configure pattern layout for log messages
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n");
        encoder.start();

        ConsoleAppender consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        FileAppender fileAppender = new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setFile("logs/consumer.log");
        fileAppender.setEncoder(encoder);
        fileAppender.start();

        //Set up root logger
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);
    }
}
