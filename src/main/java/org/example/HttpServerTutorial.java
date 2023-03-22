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
    /* rename newgame to start
    add validation to this class here
    fix logger, use variable? and print to console?
    temporarily diable guesscounter
    do a statistics variable etc
    refaktrrrrr
     */

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
        public void handle(HttpExchange exchange) {
            try {
                doHandle(exchange);
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

        private void handleGuess(HttpExchange exchange) throws IOException {
            String guess = getRequestPayload(exchange);

            if (guess.isEmpty()) {
                sendResponse(exchange, "Not valid input. Try something else!", 400);
            } else {
                try {

                    validateGameStarted();
                    int intGuess = getAsInt(guess);
                    validateRange(intGuess);

                    sendResponse(exchange, String.valueOf(numbersGame.guess(intGuess)), 200);
                } catch (RuntimeException e) {
                    sendResponse(exchange, e.getMessage(), 400);
                }
            }
        }

        private void validateGameStarted() {
            if (!numbersGame.gameStarted) {
                throw new RuntimeException("No game to play at the moment!");
            }
        }

        private static int getAsInt(String input) {
            try {
                return Integer.parseInt(input);
            } catch (Exception e) {
                throw new RuntimeException("This is not a number!");
            }
        }

        private void validateRange(int input) {
            if (input < 1 || input > 100) {
                throw new RuntimeException("Number must be between 1 and 100!");
            }
        }

        private void handleNewGame(HttpExchange exchange) throws IOException {
            if (numbersGame.gameStarted) {
                sendResponse(exchange, "", 400);
                return;
            }
            numbersGame.start();
            sendResponse(exchange, "", 200);
        }

        private void handleEndGame(HttpExchange exchange) throws IOException {
            if (!numbersGame.gameStarted) {
                sendResponse(exchange, "", 400);
                return;
            }
            numbersGame.end();
            sendResponse(exchange, "", 200);
        }

        private void handleNotFound(HttpExchange exchange) throws IOException {
            sendResponse(exchange, getHtml("res/NotFound.html"), 404);
        }

        private void sendResponse(HttpExchange exchange, String response, int responseCode) throws IOException {
            //logRequest(LocalDateTime.now(), exchange, responseCode);
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
            String log = "[" + date + "] " + exchange.getRequestMethod() + " " + exchange.getRequestURI() + " -> " + responseCode + getRequestPayload(exchange) + "\n";
            fos.write(log.getBytes());
            fos.close();
        }

        private String getRequestPayload(HttpExchange exchange) throws IOException {
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