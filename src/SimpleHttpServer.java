import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class SimpleHttpServer {

    private static final int PORT = 8080; // Puerto del servidor
    private static final String WEB_ROOT = "web"; // Directorio con archivos estáticos
    private static Banco banco; // Instancia de la clase Banco

    public static void main(String[] args) throws IOException {
        banco = new Banco(); // Inicializa la instancia de Banco
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(PORT), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/balance", new BalanceHandler());
        server.createContext("/deposit", new DepositHandler());
        server.createContext("/withdraw", new WithdrawHandler());
        server.createContext("/transfer", new TransferHandler());
        server.createContext("/logout", new LogoutHandler());
        server.setExecutor(null); // Crear un ejecutor predeterminado
        server.start();
        System.out.println("Servidor HTTP iniciado en http://localhost:" + PORT);
    }

    // Manejador de archivos estáticos
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html"; // Redirigir a index.html
            }
            Path filePath = Paths.get(WEB_ROOT, path);
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String contentType = Files.probeContentType(filePath);
                byte[] content = Files.readAllBytes(filePath);
                exchange.getResponseHeaders().set("Content-Type", contentType != null ? contentType : "application/octet-stream");
                exchange.sendResponseHeaders(200, content.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content);
                }
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    // Manejador de inicio de sesión
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String[] params = requestBody.split("&");
                String username = null;
                String password = null;

                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        if (key.equals("username")) {
                            username = value;
                        } else if (key.equals("password")) {
                            password = value;
                        }
                    }
                }

                Cliente cliente = banco.buscarCliente(username);
                String response;
                int responseCode;
                if (cliente != null && cliente.verificarContrasena(password)) {
                    response = "{\"message\": \"Inicio de sesión exitoso. Bienvenido " + cliente.getNombre() + "!\"}";
                    responseCode = 200;
                } else {
                    response = "{\"error\": \"Nombre de usuario o contraseña incorrectos.\"}";
                    responseCode = 401;
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(responseCode, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                String response = "{\"error\": \"Método no permitido.\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(405, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    // manejador de registro de usuario
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String[] params = requestBody.split("&");
                String username = null;
                String accountType = null;
                String password = null;
                double saldo = 0.0;

                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        switch (key) {
                            case "username":
                                username = value;
                                break;
                            case "accountType":
                                accountType = value;
                                break;
                            case "password":
                                password = value;
                                break;
                            case "saldo":
                                try {
                                    saldo = Double.parseDouble(value);
                                } catch (NumberFormatException e) {
                                    saldo = 0.0; // Valor predeterminado en caso de error
                                }
                                break;
                        }
                    }
                }

                String response;
                int responseCode;

                // Validar saldo
                if (saldo < 0) {
                    response = "{\"status\": \"error\", \"error\": \"El saldo no puede ser negativo.\"}";
                    responseCode = 400;
                } else if (banco.buscarCliente(username) == null) {
                    var nuevoCliente = new Cliente(username, accountType, saldo, password);
                    banco.agregarCliente(nuevoCliente);
                    response = "{\"status\": \"success\", \"message\": \"Registro exitoso. Bienvenido/a " + nuevoCliente.getNombre() + "!\"}";
                    responseCode = 200;
                } else {
                    response = "{\"status\": \"error\", \"error\": \"El nombre de usuario ya está en uso.\"}";
                    responseCode = 400;
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(responseCode, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                String response = "{\"status\": \"error\", \"error\": \"Método no permitido.\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(405, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    //manejador de checkear saldo
    static class BalanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String username = URLDecoder.decode(requestBody.split("=")[1], StandardCharsets.UTF_8);
                Cliente cliente = banco.buscarCliente(username);

                String response;
                int responseCode;
                if (cliente != null) {
                    // Devuelve el saldo en formato JSON
                    response = "{\"saldo\": " + cliente.getSaldo() + "}";
                    responseCode = 200;
                } else {
                    // Si no se encuentra el cliente, devuelve un error
                    response = "{\"error\": \"Cliente no encontrado.\"}";
                    responseCode = 404;
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(responseCode, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                String response = "{\"error\": \"Método no permitido.\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(405, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    //manejador de deposito
    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("Cuerpo de la solicitud: " + requestBody);
                String[] params = requestBody.split("&");
                String username = null;
                double amount = 0.0;

                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        if (key.equals("username")) {
                            username = value;
                        } else if (key.equals("amount")) {
                            try {
                                amount = Double.parseDouble(value);
                            } catch (NumberFormatException e) {
                                amount = 0.0; // Valor predeterminado en caso de error
                            }
                        }
                    }
                }

                Cliente cliente = banco.buscarCliente(username);
                String response;
                int responseCode;

                if (cliente != null) {
                    cliente.depositar(amount);
                    response = "{\"message\": \"Depósito exitoso. Nuevo saldo: " + cliente.getSaldo() + "\"}";
                    responseCode = 200;
                } else {
                    response = "{\"error\": \"Cliente no encontrado.\"}";
                    responseCode = 404;
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(responseCode, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                String response = "{\"error\": \"Método no permitido.\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(405, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    //manejador de retiros
    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String[] params = requestBody.split("&");
                String username = null;
                double amount = 0;

                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        if ("username".equals(key)) {
                            username = value;
                        } else if ("amount".equals(key)) {
                            try {
                                amount = Double.parseDouble(value);
                            } catch (NumberFormatException e) {
                                amount = 0.0;
                            }
                        }
                    }
                }

                String response;
                int responseCode;

                if (username != null && amount > 0) {
                    Cliente cliente = banco.buscarCliente(username);
                    if (cliente != null) {
                        if (cliente.getSaldo() >= amount) {
                            cliente.retirar(amount);
                            response = "{\"message\": \"Retiro exitoso. Saldo restante: $" + cliente.getSaldo() + "\"}";
                            responseCode = 200;
                        } else {
                            response = "{\"error\": \"Saldo insuficiente.\"}";
                            responseCode = 400;
                        }
                    } else {
                        response = "{\"error\": \"Usuario no encontrado.\"}";
                        responseCode = 404;
                    }
                } else {
                    response = "{\"error\": \"Solicitud inválida. Revisa los datos enviados.\"}";
                    responseCode = 400;
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(responseCode, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                String response = "{\"error\": \"Método no permitido.\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(405, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    //manejador de transferencias
    public static class TransferHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String[] params = requestBody.split("&");
                String fromUsername = null;
                String toUsername = null;
                double amount = 0;

                // Extraer y decodificar parámetros
                try {
                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2) {
                            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                            String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                            if ("fromUsername".equals(key)) {
                                fromUsername = value;
                            } else if ("toUsername".equals(key)) {
                                toUsername = value;
                            } else if ("amount".equals(key)) {
                                amount = Double.parseDouble(value); // Puede lanzar NumberFormatException
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    sendErrorResponse(exchange, 400, "Error al parsear la cantidad.");
                    return;
                }

                // Validar parámetros
                if (fromUsername == null || toUsername == null || amount <= 0) {
                    sendErrorResponse(exchange, 400, "Solicitud inválida. Revisa los datos enviados.");
                    return;
                }

                // Buscar los clientes y procesar la transferencia
                try {
                    Cliente fromClient = banco.buscarCliente(fromUsername);
                    Cliente toClient = banco.buscarCliente(toUsername);

                    if (fromClient != null && toClient != null) {
                        fromClient.transferir(toClient, amount);
                        String response = "{\"message\": \"Transferencia exitosa. Saldo restante: $" + fromClient.getSaldo() + "\"}";
                        sendResponse(exchange, 200, response);
                    } else {
                        sendErrorResponse(exchange, 404, "Usuario remitente o destinatario no encontrado.");
                    }
                } catch (IllegalArgumentException e) {
                    sendErrorResponse(exchange, 400, e.getMessage());
                } catch (IllegalStateException e) {
                    sendErrorResponse(exchange, 400, e.getMessage());
                }
            } else {
                sendErrorResponse(exchange, 405, "Método no permitido.");
            }
        }

        //  auxiliar para enviar respuestas con error
        private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            String response = "{\"error\": \"" + message + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }

        // auxiliar para enviar respuestas exitosas
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    //manejador de cierre de sesion
    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                String response = "{\"status\": \"success\", \"message\": \"Sesión cerrada exitosamente.\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                String response = "{\"status\": \"error\", \"error\": \"Método no permitido.\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(405, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

}
