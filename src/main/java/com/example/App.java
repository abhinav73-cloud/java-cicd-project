
package com.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class App {

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public double divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return (double) a / b;
    }

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", 8080), 0);

        App calculator = new App();

        server.createContext("/", exchange -> {
            String result = "";

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String body = new String(
                            exchange.getRequestBody().readAllBytes(),
                            StandardCharsets.UTF_8);

                    Map<String, String> data = parseForm(body);

                    int num1 = Integer.parseInt(data.get("num1"));
                    int num2 = Integer.parseInt(data.get("num2"));
                    String operation = data.get("operation");

                    switch (operation) {
                        case "add":
                            result = String.valueOf(calculator.add(num1, num2));
                            break;
                        case "subtract":
                            result = String.valueOf(calculator.subtract(num1, num2));
                            break;
                        case "multiply":
                            result = String.valueOf(calculator.multiply(num1, num2));
                            break;
                        case "divide":
                            result = String.valueOf(calculator.divide(num1, num2));
                            break;
                        default:
                            result = "Invalid operation";
                    }
                } catch (ArithmeticException e) {
                    result = "Error: " + e.getMessage();
                } catch (Exception e) {
                    result = "Please enter valid numbers.";
                }
            }

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Java CI/CD Calculator</title>
                        <meta name="viewport" content="width=device-width, initial-scale=1">
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background: #eef2ff;
                                display: flex;
                                justify-content: center;
                                align-items: center;
                                min-height: 100vh;
                                margin: 0;
                            }
                            .calculator {
                                background: white;
                                padding: 30px;
                                border-radius: 15px;
                                box-shadow: 0 5px 20px #0002;
                                width: 320px;
                                text-align: center;
                            }
                            h1 { color: #263b80; }
                            input, select, button {
                                box-sizing: border-box;
                                width: 100%;
                                padding: 12px;
                                margin: 10px 0;
                                border-radius: 7px;
                                border: 1px solid #ccc;
                                font-size: 16px;
                            }
                            button {
                                background: #3155cc;
                                color: white;
                                border: none;
                                cursor: pointer;
                            }
                            button:hover { background: #203b9c; }
                            .result {
                                margin-top: 15px;
                                color: #167342;
                                font-weight: bold;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="calculator">
                            <h1>Java Calculator</h1>
                            <p>Java CI/CD Web Application</p>
                            <form method="POST" action="/">
                                <input type="number" name="num1"
                                       placeholder="First number" required>
                                <input type="number" name="num2"
                                       placeholder="Second number" required>
                                <select name="operation">
                                    <option value="add">Addition (+)</option>
                                    <option value="subtract">Subtraction (-)</option>
                                    <option value="multiply">Multiplication (*)</option>
                                    <option value="divide">Division (/)</option>
                                </select>
                                <button type="submit">Calculate</button>
                            </form>
                            <div class="result">%s</div>
                        </div>
                    </body>
                    </html>
                    """.formatted(escapeHtml(result));

            sendResponse(exchange, 200, html, "text/html; charset=UTF-8");
        });

        server.createContext("/health", exchange -> {
            sendResponse(exchange, 200, "Application is healthy!", "text/plain");
        });

        server.setExecutor(null);
        server.start();

        System.out.println("Calculator website running on port 8080");
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> data = new HashMap<>();

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {
                data.put(
                        URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                );
            }
        }

        return data;
    }

    private static void sendResponse(
            HttpExchange exchange, int status, String response, String contentType)
            throws IOException {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
