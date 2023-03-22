package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;


public class HttpServerTutorial {

    static NumbersGame numbersGame = new NumbersGame();

    public static String getHtml(String path) throws IOException {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        InetSocketAddress addr = new InetSocketAddress("localhost", 5555);
        HttpServer httpServer = HttpServer.create(addr, 0);
        httpServer.createContext("/", new RequestHandler());
        httpServer.start();
        System.out.println("Server started, listening at: " + addr);
    }

    private static class RequestHandler implements HttpHandler {
        private String userInput;

        @Override
        public void handle(HttpExchange exchange) {
            try {
                doHandle(exchange);
                logRequest(LocalDateTime.now(), exchange, exchange.getResponseCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void doHandle(HttpExchange exchange) throws IOException {
            String methodAndPath = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath();

            switch (methodAndPath) {
                case "POST /start-game" -> handleNewGame(exchange);
                case "POST /guess" -> handleGuess(exchange);
                case "POST /end-game" -> handleEndGame(exchange);
                default -> handleNotFound(exchange);
            }
        }

        private void handleNewGame(HttpExchange exchange) throws IOException {
            userInput = "start";
            if (numbersGame.gameStarted) {
                sendResponse(exchange, "", 400);
            } else {
                numbersGame.start();
                sendResponse(exchange, "", 200);
            }
        }

        private void handleGuess(HttpExchange exchange) throws IOException {
            String guess = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            userInput = guess;
            try {
                validateEmptyInput(guess);
                validateGameStarted();
                int intGuess = getAsInt(guess);
                validateRange(intGuess);

                sendResponse(exchange, numbersGame.guess(intGuess).toString(), 200);
            } catch (RuntimeException e) {
                sendResponse(exchange, e.getMessage(), 400);
            }
        }

        private void handleEndGame(HttpExchange exchange) throws IOException {
            userInput = "exit";
            if (!numbersGame.gameStarted) {
                sendResponse(exchange, "", 400);
            } else {
                numbersGame.end();
                sendResponse(exchange, "", 200);
            }
        }

        private void handleNotFound(HttpExchange exchange) throws IOException {
            sendResponse(exchange, getHtml("res/NotFound.html"), 404);
        }

        private void validateEmptyInput(String guess) {
            if (guess.isEmpty()) throw new RuntimeException("Not valid input. Try something else!");
        }

        private void validateGameStarted() {
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

        private void sendResponse(HttpExchange exchange, String response, int responseCode) throws IOException {
            exchange.sendResponseHeaders(responseCode, response.length());
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response.getBytes());
                outputStream.flush();
            }
        }

        private void logRequest(LocalDateTime date, HttpExchange exchange, int responseCode) throws IOException {
            File file = new File("log.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            String log = "[" + date + "] " + exchange.getRequestMethod() + " " + exchange.getRequestURI() + " -> " + responseCode + " " + userInput + "\n";
            System.out.println(log);
            fos.write(log.getBytes());
            fos.close();
        }
    }
}