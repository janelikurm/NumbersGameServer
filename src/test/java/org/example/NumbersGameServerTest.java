package org.example;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.example.NumbersGameServer.RequestHandler.RESPONSE_HEADER_SESSION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class NumbersGameServerTest {

    public static final String API_URL = "http://10.10.10.25:5555";
    public String stopNumberGameGuessing = "";
    static String sessionId = "";


    @Test
    void gameStatus() {
        startSessionViaLoginRequest();
        assertEquals(200, doRequest("/status", "", "GET"));
    }

    @Test
    void startGameTest() {
        startSessionViaLoginRequest();
        assertEquals(200, doRequest("/start-game", "", "POST"));
        assertEquals(400, doRequest("/start-game", "", "POST"));
    }

    @Test
    void startGameWithWrongId() {
        sessionId = "invalidSessionId";
        assertEquals(401, doRequest("/start-game", "", "POST"));
        sessionId = "389723730983";
        assertEquals(401, doRequest("/start-game", "", "POST"));
    }

    @Test
    void guessNumberCorrectInput() {
        startGame();
        for (int i = 1; i <= 100; i++) {
            if (stopNumberGameGuessing.equals("LESS")) {
                assertEquals("LESS", doRequestAndReturnBody("/guess", String.valueOf(i), "POST"));
            } else {
                break;
            }
        }
    }

    @Test
    void guessNumberOutOfBoundInput() {
        assertEquals(400, doRequest("/guess", "-10", "POST"));
        assertEquals(400, doRequest("/guess", "1000", "POST"));
        assertEquals(400, doRequest("/guess", "0", "POST"));
        sessionId = "invalidSessionId";
        assertEquals(401, doRequest("/guess", "", "POST"));
    }

    @Test
    void guessNumberStringInput() {
        startSessionViaLoginRequest();
        assertEquals(400, doRequest("/guess", "string", "POST"));
        assertEquals(400, doRequest("/guess", "st68ng", "POST"));
    }

    @Test
    void endGame() {
        startSessionViaLoginRequest();
        startGame();
        assertEquals(200, doRequest("/end-game", "", "POST"));
        assertEquals(400, doRequest("/end-game", "", "POST"));
        sessionId = "invalidSessionId";
        assertEquals(401, doRequest("/end-game", "", "POST"));
    }

    private void startGame() {
        doRequest("/start-game", "", "POST");
    }

    private void startSessionViaLoginRequest() {
        doRequest("/login", "", "POST");

    }

    private int doRequest(String endPoint, String body, String requestMethod) {
        URI HTTP_SERVER_URI = URI.create(API_URL + endPoint);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .method(requestMethod, HttpRequest.BodyPublishers.ofString(body))
                .uri(HTTP_SERVER_URI)
                .header(RESPONSE_HEADER_SESSION_ID, sessionId)
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            sessionId = response.headers().allValues(RESPONSE_HEADER_SESSION_ID).get(0);
            return response.statusCode();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String doRequestAndReturnBody(String endPoint, String body, String requestMethod) {

        URI HTTP_SERVER_URI = URI.create(API_URL + endPoint);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .method(requestMethod, HttpRequest.BodyPublishers.ofString(body))
                .uri(HTTP_SERVER_URI)
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            stopNumberGameGuessing = responseBody;
            return responseBody;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}