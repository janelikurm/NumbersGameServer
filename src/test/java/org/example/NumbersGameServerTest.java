package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class NumbersGameServerTest {

    public String stopNumberGameGuessing = "";

    @BeforeEach
    public void stopGame() {
        doRequest("/end-game", "", "POST");
    }

    @Test
    void gameStatus() {
        assertEquals(200, doRequest("/status", "", "GET"));
        startGame();
        assertEquals(200, doRequest("/status", "", "GET"));

    }

    @Test
    void startGame() {
        assertEquals(200, doRequest("/start-game", "", "POST"));
        assertEquals(400, doRequest("/start-game", "", "POST"));
    }

    @Test
    void guessNumberGood() {
        runGame();
        for (int i = 1; i <= 100; i++) {
            if (stopNumberGameGuessing.equals("LESS")) {
                assertEquals("LESS", doRequestAndReturnBody("/guess", String.valueOf(i), "POST"));
            } else {
                break;
            }
        }
    }

    @Test
    void guessNumberBad() {
        assertEquals(400, doRequest("/guess", "-10", "POST"));
        assertEquals(400, doRequest("/guess", "1000", "POST"));
        assertEquals(400, doRequest("/guess", "0", "POST"));
    }

    @Test
    void guessNumberStringInput() {
        assertEquals(400, doRequest("/guess", "string", "POST"));
        assertEquals(400, doRequest("/guess", "st68ng", "POST"));
    }

    @Test
    void endGame() {
        runGame();
        assertEquals(200, doRequest("/end-game", "", "POST"));
        assertEquals(400, doRequest("/end-game", "", "POST"));
    }

    public void runGame() {
        doRequest("/start-game", "", "POST");

    }

    private int doRequest(String endPoint, String body, String requestMethod) {

        URI HTTP_SERVER_URI = URI.create("http://10.10.10.156:5555" + endPoint);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .method(requestMethod, HttpRequest.BodyPublishers.ofString(body))
                .uri(HTTP_SERVER_URI)
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String doRequestAndReturnBody(String endPoint, String body, String requestMethod) {

        URI HTTP_SERVER_URI = URI.create("http://10.10.10.156:5555" + endPoint);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}