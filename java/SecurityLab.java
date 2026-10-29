import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.OutputStream;

public class SecurityLab {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "8080")
        );

        HttpServer server = HttpServer.create(
            new InetSocketAddress("0.0.0.0", port), 0
        );

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();

            String file;
            String contentType;

            if (path.equals("/") || path.equals("/index.html")) {
                file = "index.html";
                contentType = "text/html; charset=UTF-8";
            } else if (path.equals("/style.css")) {
                file = "style.css";
                contentType = "text/css; charset=UTF-8";
            } else {
                byte[] error = "404 - Not Found".getBytes(
                    StandardCharsets.UTF_8
                );
                exchange.sendResponseHeaders(404, error.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(error);
                }
                return;
            }

            try {
                byte[] data = Files.readAllBytes(Path.of(file));

                exchange.getResponseHeaders().set(
                    "Content-Type", contentType
                );
                exchange.getResponseHeaders().set(
                    "X-Content-Type-Options", "nosniff"
                );
                exchange.getResponseHeaders().set(
                    "X-Frame-Options", "DENY"
                );
                exchange.getResponseHeaders().set(
                    "Referrer-Policy", "no-referrer"
                );

                exchange.sendResponseHeaders(200, data.length);

                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(data);
                }
            } catch (IOException e) {
                byte[] error = "Server error".getBytes(
                    StandardCharsets.UTF_8
                );
                exchange.sendResponseHeaders(500, error.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(error);
                }
            }
        });

        server.start();
        System.out.println("Security Lab running on port " + port);
    }
}