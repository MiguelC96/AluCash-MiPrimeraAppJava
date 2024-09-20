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
        server.createContext("/register", new RegisterHandler()); // Manejador para registro
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

    // Manejador de registro de usuario
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
                        if (key.equals("username")) {
                            username = value;
                        } else if (key.equals("accountType")) {
                            accountType = value;
                        } else if (key.equals("password")) {
                            password = value;
                        } else if (key.equals("saldo")) {
                            try {
                                saldo = Double.parseDouble(value);
                            } catch (NumberFormatException e) {
                                saldo = 0.0; // Valor predeterminado en caso de error
                            }
                        }
                    }
                }

                String response;
                int responseCode;
                if (banco.buscarCliente(username) == null) {
                    var nuevoCliente = new Cliente(username, accountType, saldo, password);
                    banco.agregarCliente(nuevoCliente);
                    response = "{\"message\": \"Registro exitoso. Bienvenido " + nuevoCliente.getNombre() + "!\"}";
                    responseCode = 200;
                } else {
                    response = "{\"error\": \"El nombre de usuario ya está en uso.\"}";
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
}