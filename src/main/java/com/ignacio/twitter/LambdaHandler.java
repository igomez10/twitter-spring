package com.ignacio.twitter;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * AWS Lambda handler for Spring Boot application.
 * Manages cold start initialization and proxies HTTP requests to Spring Boot.
 */
public class LambdaHandler implements RequestStreamHandler {
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static boolean initialized = false;

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        // Initialize on cold start
        if (!initialized) {
            initializeSecrets(context);
            initializeSpringBootHandler(context);
            initialized = true;
        }

        // Handle the request
        handler.proxyStream(input, output, context);
    }

    /**
     * Load secrets from AWS Secrets Manager and set as system properties.
     */
    private static void initializeSecrets(Context context) {
        String secretArn = System.getenv("AWS_SECRETS_ARN");
        if (secretArn == null || secretArn.isEmpty()) {
            context.getLogger().log("WARNING: AWS_SECRETS_ARN not set, using environment variables");
            return;
        }

        try (SecretsManagerClient secretsClient = SecretsManagerClient.builder().build()) {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretArn)
                    .build();

            GetSecretValueResponse response = secretsClient.getSecretValue(request);
            String secretString = response.secretString();

            @SuppressWarnings("unchecked")
            Map<String, String> secrets = objectMapper.readValue(secretString, Map.class);

            // Set database configuration
            if (secrets.containsKey("DATABASE_URL")) {
                System.setProperty("SPRING_DATASOURCE_URL", secrets.get("DATABASE_URL"));
            }
            if (secrets.containsKey("DATABASE_USERNAME")) {
                System.setProperty("SPRING_DATASOURCE_USERNAME", secrets.get("DATABASE_USERNAME"));
            }
            if (secrets.containsKey("DATABASE_PASSWORD")) {
                System.setProperty("SPRING_DATASOURCE_PASSWORD", secrets.get("DATABASE_PASSWORD"));
            }
            if (secrets.containsKey("JWT_SECRET")) {
                System.setProperty("JWT_SECRET", secrets.get("JWT_SECRET"));
            }
            if (secrets.containsKey("JWT_TTL_SECONDS")) {
                System.setProperty("JWT_TTL_SECONDS", secrets.get("JWT_TTL_SECONDS"));
            }

            context.getLogger().log("Secrets loaded successfully from Secrets Manager");
        } catch (Exception e) {
            context.getLogger().log("ERROR loading secrets: " + e.getMessage());
            throw new RuntimeException("Failed to load secrets from Secrets Manager", e);
        }
    }

    /**
     * Initialize Spring Boot Lambda container handler.
     */
    private static void initializeSpringBootHandler(Context context) {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(TwitterApplication.class);
            context.getLogger().log("Spring Boot Lambda handler initialized successfully");
        } catch (Exception e) {
            context.getLogger().log("ERROR initializing Spring Boot handler: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Spring Boot Lambda handler", e);
        }
    }
}
