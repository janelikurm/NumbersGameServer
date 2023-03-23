package org.example;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class NumbersGameServerTest {

//    NumbersGameServer numbersGameServer = new NumbersGameServer();
//
//    String baseUri = "http://10.10.10.25:5555/";

    private static int doRequest(String endPoint, String body, String requestMethod) {

        URI HTTP_SERVER_URI = URI.create("http://10.10.10.25:5555" + endPoint);
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

    @Test
    @Order(1)
    void startGameIfNotStarted() {
    assertEquals(200, doRequest("/start-game", "", "POST"));
    }

    @Test
    void startGameIfStarted () {
    assertEquals(400, doRequest("/start-game", "", "POST"));
    }

    @Test
    void guessNumberGood() {
        assertEquals(200, doRequest("/guess", "10", "POST"));
        assertEquals(200, doRequest("/guess", "100", "POST"));
        assertEquals(200, doRequest("/guess", "1", "POST"));
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
    void endGameIfNotEnded() {
        assertEquals(200, doRequest("/end-game", "", "POST"));
    }

    //        HttpResponse httpResponse = client.send("/start-game");
//
//        assertEquals(200, httpResponse.statusCode);

}