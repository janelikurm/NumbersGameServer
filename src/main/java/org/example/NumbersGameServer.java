package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class NumbersGameServer {


    public static String getHtml(String path) throws IOException {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        InetSocketAddress addr = new InetSocketAddress("0.0.0.0", 5555);
        HttpServer httpServer = HttpServer.create(addr, 0);
        httpServer.createContext("/", new RequestHandler());
        httpServer.start();
        System.out.println("Server started, listening at: " + addr);
    }

    static class RequestHandler implements HttpHandler {

        static final String RESPONSE_HEADER_SESSION_ID = "X-SESSION-ID";
        private String userInput = "";

        Map<String, Map<String, Object>> sessions = new HashMap<>();
        Map<String, Object> gameSession = new HashMap<>();


        @Override
        public void handle(HttpExchange exchange) {


            try {
                doHandle(exchange);
                logRequest(LocalDateTime.now(), exchange, exchange.getResponseCode());
                userInput = "";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void doHandle(HttpExchange exchange) throws IOException {
            String methodAndPath = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath();
            String sessionId = exchange.getRequestHeaders().getFirst(RESPONSE_HEADER_SESSION_ID);
            if (methodAndPath.equals("POST /login")) {
                handleLogin(exchange);
            } else if (sessionId == null || !(sessions.containsKey(sessionId))) {
                sendResponse(exchange, "", 401, sessionId);
            } else {
                NumbersGame numbersGame = (NumbersGame) sessions.get(sessionId).get("game");
                try {
                    switch (methodAndPath) {
                        case "POST /start-game" -> handleNewGame(exchange, numbersGame, sessionId);
                        case "POST /guess" -> handleGuess(exchange, numbersGame, sessionId);
                        case "POST /end-game" -> handleEndGame(exchange, numbersGame, sessionId);
                        case "GET /stats" -> handleStats(exchange, numbersGame, sessionId);
                        case "GET /status" -> handleStatus(exchange, numbersGame, sessionId);
                        default -> handleNotFound(exchange, sessionId);
                    } findSessionAndUpdateTime(sessionId);
                } catch (Exception e) {
                    sendResponse(exchange, e.getMessage(), 401, sessionId);
                }
            }
        }

        private boolean checkSessionExpiration(String sessionId) {
            Session currentSession = (Session) sessions.get(sessionId).get("session");
            boolean isExpired = currentSession.isExpired();
            System.out.println(isExpired);
            return isExpired;
        }

        private void findSessionAndUpdateTime(String sessionId) {
            Session currentSession = (Session) sessions.get(sessionId).get("session");
            currentSession.lastActiveTime();
        }

        private void handleLogin(HttpExchange exchange) throws IOException {
            NumbersGame numbersGame = new NumbersGame();
            Session session = new Session();
            String sessionId = String.valueOf(UUID.randomUUID());

            gameSession.put("game", numbersGame);
            gameSession.put("session", session);
            sessions.put(sessionId, gameSession);
            sendResponse(exchange, "", 200, sessionId);
            findSessionAndUpdateTime(sessionId);
        }

        private void handleStats(HttpExchange exchange, NumbersGame numbersGame, String sessionId) throws IOException {
            sendResponse(exchange, numbersGame.stats.toString(), 200, sessionId);
        }

        private void handleStatus(HttpExchange exchange, NumbersGame numbersGame, String sessionId) throws IOException {
            sendResponse(exchange, numbersGame.gameStarted ? "Game in progress" : "No game in progress", 200, sessionId);
        }


        private void handleNewGame(HttpExchange exchange, NumbersGame numbersGame, String sessionId) throws IOException {
            boolean isExpired = checkSessionExpiration(sessionId);

            if (!isExpired) {
                if (numbersGame.gameStarted) {
                    sendResponse(exchange, "", 400, sessionId);
                } else {
                    numbersGame.start();
                    sendResponse(exchange, "", 200, sessionId);
                }
            } else {
                throw new RuntimeException("Session expired");
            }
        }

        private void handleGuess(HttpExchange exchange, NumbersGame numbersGame, String sessionId) throws IOException {
            userInput = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            boolean isExpired = checkSessionExpiration(sessionId);
            if (!isExpired) {
                try {
                    validateEmptyInput(userInput);
                    validateGameStarted(numbersGame);
                    int intGuess = getAsInt(userInput);
                    validateRange(intGuess);

                    sendResponse(exchange, numbersGame.guess(intGuess).toString(), 200, sessionId);
                } catch (RuntimeException e) {
                    sendResponse(exchange, e.getMessage(), 400, sessionId);
                }
            } else {
                throw new RuntimeException("Session expired");
            }
        }

        private void handleEndGame(HttpExchange exchange, NumbersGame numbersGame, String sessionId) throws IOException {
            if (!numbersGame.gameStarted) {
                sendResponse(exchange, "", 400, sessionId);
            } else {
                numbersGame.end();
                sendResponse(exchange, "", 200, sessionId);
            }
        }

        private void handleNotFound(HttpExchange exchange, String sessionId) throws IOException {
            userInput = exchange.getRequestURI().getPath();
            sendResponse(exchange, getHtml("src/res/NotFound.html"), 404, sessionId);
        }

        private void validateEmptyInput(String guess) {
            if (guess.isEmpty()) throw new RuntimeException("Not valid input. Try something else!");
        }

        private void validateGameStarted(NumbersGame numbersGame) {

            if (!numbersGame.gameStarted) throw new RuntimeException("No game to play at the moment!");
        }

        private static int getAsInt(String input) {
            try {
                return Integer.parseInt(input);
            } catch (Exception e) {
                throw new RuntimeException("This is not a number!");
            }
        }

        private void validateRange(int input) {
            if (input < 1 || input > 100) throw new RuntimeException("Number must be between 1 and 100!");
        }

        private void sendResponse(HttpExchange exchange, String response, int responseCode, String sessionId) throws IOException {
            exchange.getResponseHeaders().set(RESPONSE_HEADER_SESSION_ID, sessionId);

            exchange.sendResponseHeaders(responseCode, response.length());
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response.getBytes());
                outputStream.flush();
            }
        }

        private void logRequest(LocalDateTime date, HttpExchange exchange, int responseCode) throws IOException {
            String sessionId = exchange.getResponseHeaders().getFirst(RESPONSE_HEADER_SESSION_ID);
            File file = new File("log.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            String log = "[" + date.format(formatter) + "] " +
                    exchange.getRequestMethod() + " " +
                    exchange.getRequestURI() + " -> " +
                    responseCode + " " +
                    (userInput.length() != 0 ? "'" + userInput + "'" : userInput) + "\n" +
                    "SessionId: " + sessionId;
            System.out.println(log);
            fos.write(log.getBytes());
            fos.close();
        }
    }
}