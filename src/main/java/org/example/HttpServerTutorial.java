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

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            switch (exchange.getRequestURI().getPath()) {
                case "/start-game" -> {
                    switch (exchange.getRequestMethod()) {
                        case "POST" -> handleNewGame(exchange);
                        default -> handleNotFound(exchange);
                    }
                }
                case "/guess" -> {
                    switch (exchange.getRequestMethod()) {
                        case "POST" -> handleGuess(exchange);
                        default -> handleNotFound(exchange);
                    }
                }
                case "/end-game" -> {
                    switch (exchange.getRequestMethod()) {
                        case "POST" -> handleEndGame(exchange);
                        default -> handleNotFound(exchange);
                    }
                }
                default -> handleNotFound(exchange);
            }
        }

        private static void handleGuess(HttpExchange exchange) throws IOException {
            String guess = getRequestPayload(exchange);
            String numbersGameResponse = "";

            if (!guess.equals("")) {
                try {
                    numbersGameResponse = numbersGame.guess(guess) + " " + numbersGame.guessCounter;
                    sendResponse(exchange, numbersGameResponse, 200);
                } catch (RuntimeException e) {
                    sendResponse(exchange, e.getMessage(), 400);
                }
            }else{
                sendResponse(exchange, numbersGameResponse, 200);
            }
        }

        private static void handleNewGame(HttpExchange exchange) throws IOException {
            if (numbersGame.gameStarted) {
                sendResponse(exchange, "", 400);
                return;
            }
            numbersGame.newGame();
            sendResponse(exchange, "", 200);
        }

        private static void handleEndGame(HttpExchange exchange) throws IOException {
            if (!numbersGame.gameStarted) {
                sendResponse(exchange, "", 400);
                return;
            }
            numbersGame.endGame();
            sendResponse(exchange, "", 200);
        }

        private static void handleNotFound(HttpExchange exchange) throws IOException {
            sendResponse(exchange, getHtml("res/NotFound.html"), 404);
        }

        private static void sendResponse(HttpExchange exchange, String response, int responseCode) throws IOException {
            logRequest(LocalDateTime.now(), exchange, responseCode);
            exchange.sendResponseHeaders(responseCode, response.length());
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response.getBytes());
                outputStream.flush();
            }
        }

        private static void logRequest(LocalDateTime date, HttpExchange exchange, int responseCode) throws IOException {
            File file = new File("log.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            String log = "[" + date + "] " + exchange.getRequestMethod() + " " + exchange.getRequestURI() + " -> " + responseCode + "\n";//+ getRequestPayload(exchange) + "\n";
            fos.write(log.getBytes());
            fos.close();
        }

        private static String getRequestPayload(HttpExchange exchange) throws IOException {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            int b;
            StringBuilder buf = new StringBuilder(512);
            while ((b = br.read()) != -1) {
                buf.append((char) b);
            }

            br.close();
            isr.close();
            return buf.toString();
        }

    }
}