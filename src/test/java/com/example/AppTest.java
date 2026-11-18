
package com.example;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    private App calculator;
    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        calculator = new App();

        server = App.createServer();
        server.bind(new java.net.InetSocketAddress("0.0.0.0", 0));
        server.start();

        baseUrl = "http://127.0.0.1:"
                + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void testAddition() {
        assertEquals(15, calculator.add(10, 5));
    }

    @Test
    void testSubtraction() {
        assertEquals(5, calculator.subtract(10, 5));
    }

    @Test
    void testMultiplication() {
        assertEquals(50, calculator.multiply(10, 5));
    }

    @Test
    void testDivision() {
        assertEquals(2.0, calculator.divide(10, 5));
    }

    @Test
    void testDivisionByZero() {
        assertThrows(ArithmeticException.class,
                () -> calculator.divide(10, 0));
    }

    @Test
    void testHomePage() throws Exception {
        String response = sendRequest("GET", "/", null);

        assertTrue(response.contains("Java Calculator"));
        assertTrue(response.contains("Calculate"));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        String response = sendRequest("GET", "/health", null);

        assertEquals("Application is healthy!", response);
    }

    @Test
    void testWebAddition() throws Exception {
        String response = sendRequest(
                "POST", "/", "num1=10&num2=5&operation=add");

        assertTrue(response.contains(">15</div>"));
    }

    @Test
    void testWebSubtraction() throws Exception {
        String response = sendRequest(
                "POST", "/", "num1=10&num2=5&operation=subtract");

        assertTrue(response.contains(">5</div>"));
    }

    @Test
    void testWebMultiplication() throws Exception {
        String response = sendRequest(
                "POST", "/", "num1=10&num2=5&operation=multiply");

        assertTrue(response.contains(">50</div>"));
    }

    @Test
    void testWebDivision() throws Exception {
        String response = sendRequest(
                "POST", "/", "num1=10&num2=5&operation=divide");

        assertTrue(response.contains(">2.0</div>"));
    }

    @Test
    void testWebDivisionByZero() throws Exception {
        String response = sendRequest(
                "POST", "/", "num1=10&num2=0&operation=divide");

        assertTrue(response.contains("Error: Cannot divide by zero"));
    }

    @Test
    void testInvalidOperation() throws Exception {
        String response = sendRequest(
                "POST", "/", "num1=10&num2=5&operation=unknown");

        assertTrue(response.contains("Invalid operation"));
    }

    @Test
    void testInvalidNumbers() throws Exception {
        String response = sendRequest(
                "POST", "/", "num1=abc&num2=5&operation=add");

        assertTrue(response.contains("Please enter valid numbers."));
    }

    @Test
    void testFormWithInvalidPair() throws Exception {
        String response = sendRequest(
                "POST", "/", "invalid&num1=10&num2=5&operation=add");

        assertTrue(response.contains(">15</div>"));
    }

    private String sendRequest(
            String method, String path, String body) throws Exception {

        URL url = new URL(baseUrl + path);
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        if ("POST".equals(method)) {
            connection.setDoOutput(true);
            connection.setRequestProperty(
                    "Content-Type", "application/x-www-form-urlencoded");

            byte[] data = body.getBytes(StandardCharsets.UTF_8);

            try (var output = connection.getOutputStream()) {
                output.write(data);
            }
        }

        String response;

        try (var input = connection.getInputStream()) {
            response = new String(
                    input.readAllBytes(), StandardCharsets.UTF_8);
        }

        connection.disconnect();
        return response;
    }
}
