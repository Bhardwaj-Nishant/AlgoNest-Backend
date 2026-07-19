package com.algonest.AlgoNest_Backend.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class SupabaseAdminClient {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public SupabaseAdminClient() {
        this.restTemplate = new RestTemplate();
        this.mapper = new ObjectMapper();
    }

    /*
     * ==========================
     * Common Headers
     * ==========================
     */

    private HttpHeaders buildHeaders() {

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(serviceRoleKey.trim());
        headers.set("apikey", serviceRoleKey.trim());

        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return headers;
    }

    /*
     * ==========================
     * Check Email Exists
     * ==========================
     */

    public boolean emailExists(String email) {

        String url = supabaseUrl + "/auth/v1/admin/users?page=1&per_page=1000";

        ResponseEntity<JsonNode> response = exchange(
                url,
                HttpMethod.GET,
                null
        );

        JsonNode users = response.getBody().get("users");

        if (users == null || !users.isArray()) {
            return false;
        }

        for (JsonNode user : users) {

            if (user.has("email")
                    && email.equalsIgnoreCase(user.get("email").asText())) {

                return true;
            }
        }

        return false;
    }

    /*
     * ==========================
     * Get User Id
     * ==========================
     */

    public String getUserIdByEmail(String email) {

        String url = supabaseUrl + "/auth/v1/admin/users?page=1&per_page=1000";

        ResponseEntity<JsonNode> response = exchange(
                url,
                HttpMethod.GET,
                null
        );

        JsonNode users = response.getBody().get("users");

        if (users == null) {
            throw new RuntimeException("User not found.");
        }

        for (JsonNode user : users) {

            if (user.has("email")
                    && email.equalsIgnoreCase(user.get("email").asText())) {

                return user.get("id").asText();
            }
        }

        throw new RuntimeException("User not found.");
    }

    /*
     * ==========================
     * Create User
     * ==========================
     */

    public String createUser(
            String email,
            String password,
            String displayName
    ) {

        String url = supabaseUrl + "/auth/v1/admin/users";

        String payload = String.format("""
                {
                    "email":"%s",
                    "password":"%s",
                    "email_confirm":true,
                    "user_metadata":{
                        "name":"%s"
                    }
                }
                """,
                email,
                password,
                displayName
        );

        ResponseEntity<JsonNode> response = exchange(
                url,
                HttpMethod.POST,
                payload
        );

        return response.getBody()
                .get("id")
                .asText();
    }

    /*
     * ==========================
     * Update Password
     * ==========================
     */

    public void updateUserPassword(
            String userId,
            String newPassword
    ) {

        String url =
                supabaseUrl +
                        "/auth/v1/admin/users/" +
                        userId;

        String payload = String.format("""
                {
                    "password":"%s"
                }
                """,
                newPassword
        );

        exchange(
                url,
                HttpMethod.PUT,
                payload
        );
    }

    /*
     * ==========================
     * Delete User
     * ==========================
     */

    public void deleteUser(String userId) {

        String url =
                supabaseUrl +
                        "/auth/v1/admin/users/" +
                        userId;

        exchange(
                url,
                HttpMethod.DELETE,
                null
        );
    }

    /*
     * ==========================
     * Common Exchange Method
     * ==========================
     */

    private ResponseEntity<JsonNode> exchange(
            String url,
            HttpMethod method,
            String body
    ) {

        HttpEntity<String> entity =
                new HttpEntity<>(body, buildHeaders());

        try {

            return restTemplate.exchange(
                    url,
                    method,
                    entity,
                    JsonNode.class
            );

        } catch (HttpStatusCodeException e) {

            throw new RuntimeException(
                    e.getResponseBodyAsString(),
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Supabase request failed.",
                    e
            );
        }
    }



}