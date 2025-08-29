package com.example.hrxchallenge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class HrxChallengeApplication implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HrxChallengeApplication.class);

    // ==== Candidate info ====
    private static final String NAME   = "John Doe";
    private static final String REG_NO = "REG12347"; 
    private static final String EMAIL  = "john@example.com";

    // ==== Endpoints ====
    private static final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    private static final String FALLBACK_TEST_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    // Where we persist the chosen SQL
    private static final Path RESULT_FILE = Path.of("final-query.txt");

    private final RestTemplate restTemplate;

    public HrxChallengeApplication(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(HrxChallengeApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
    try {
        log.info("== HRX Hiring Challenge: startup flow begin ==");

        // 1) Generate webhook
        GenerateWebhookResponse gen = generateWebhook(NAME, REG_NO, EMAIL);
        if (gen == null) {
            log.error("Failed to generate webhook; aborting.");
            System.exit(1);
        }

        String webhookUrl   = defaultIfBlank(gen.webhook, FALLBACK_TEST_WEBHOOK_URL);
        String accessToken  = gen.accessToken;

        if (!StringUtils.hasText(accessToken)) {
            log.error("No accessToken received; cannot proceed.");
            System.exit(1);
        }
        log.info("Received webhook URL: {}", webhookUrl);

        // 3) Produce final SQL query
        String finalQuery = buildFinalQuery();

        // 4) Save the SQL locally
        saveFinalQuery(finalQuery);

        // 5) Submit to webhook with JWT
        boolean ok = submitFinalQuery(webhookUrl, accessToken, finalQuery);
        if (ok) {
            log.info("== Submission complete ✅ ==");
        } else {
            log.error("== Submission failed ❌ ==");
        }

        // ✅ Exit JVM after work is done
        System.exit(ok ? 0 : 1);

    } catch (Exception e) {
        log.error("Unexpected error in startup flow", e);
        System.exit(1);
    }
}

    private GenerateWebhookResponse generateWebhook(String name, String regNo, String email) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of(
                    "name",  name,
                    "regNo", regNo,
                    "email", email
            );

            HttpEntity<Map<String, String>> req = new HttpEntity<>(body, headers);
            ResponseEntity<GenerateWebhookResponse> resp = restTemplate.exchange(
                    GENERATE_WEBHOOK_URL,
                    HttpMethod.POST,
                    req,
                    GenerateWebhookResponse.class
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return resp.getBody();
            }
            log.error("generateWebhook non-2xx: status={} body={}", resp.getStatusCode(), resp.getBody());
        } catch (RestClientResponseException e) {
            log.error("generateWebhook API error: status={} body={}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("generateWebhook exception", e);
        }
        return null;
    }

    private boolean submitFinalQuery(String webhookUrl, String accessToken, String finalQuery) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", accessToken);

            Map<String, String> body = Map.of("finalQuery", finalQuery);
            HttpEntity<Map<String, String>> req = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    req,
                    String.class
            );
            log.info("Webhook response: status={} body={}", resp.getStatusCode(), resp.getBody());
            return resp.getStatusCode().is2xxSuccessful();
        } catch (RestClientResponseException e) {
            log.error("submitFinalQuery API error: status={} body={}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("submitFinalQuery exception", e);
        }
        return false;
    }

    private static boolean isLastTwoDigitsOdd(String regNo) {
        Matcher m = Pattern.compile("(\\d+)$").matcher(regNo);
        if (!m.find()) return false;
        String digits = m.group(1);
        if (digits.length() == 1) return (Character.getNumericValue(digits.charAt(0)) % 2) != 0;
        String lastTwo = digits.substring(digits.length() - 2);
        int val = Integer.parseInt(lastTwo);
        return (val % 2) != 0;
    }

    private static String buildFinalQuery() {
        return "SELECT p.AMOUNT AS SALARY, " +
       "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
       "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
       "d.DEPARTMENT_NAME " +
       "FROM PAYMENTS p " +
       "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
       "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
       "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
       "ORDER BY p.AMOUNT DESC " +
       "LIMIT 1;";

    }

    private static void saveFinalQuery(String finalQuery) {
        try {
            String stamped = "-- saved: " + Instant.now() + System.lineSeparator() + finalQuery + System.lineSeparator();
            Files.writeString(RESULT_FILE, stamped);
            String preview = finalQuery.length() > 240 ? finalQuery.substring(0, 240) + "..." : finalQuery;
            log.info("Final SQL saved to {}. Preview:\n{}", RESULT_FILE.toAbsolutePath(), preview);
        } catch (IOException e) {
            log.warn("Could not persist final query to file: {}", RESULT_FILE, e);
        }
    }

    private static String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    // === DTOs ===
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GenerateWebhookResponse {
        @JsonProperty("webhook")
        public String webhook;

        @JsonProperty("accessToken")
        public String accessToken;
    }
}
