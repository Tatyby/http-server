package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final String
            NOT_FOUND_404 = "HTTP/1.1 404 Not Found\r\n",
            OK_200 = "HTTP/1.1 200 OK\r\n";
    public static final int PARTS_LENGTH = 3;
    public static final String
            CONTENT_LENGTH = "Content-Length: ",
            CONTENT_LENGTH_0 = "Content-Length: 0\r\n",
            CONTENT_TYPE = "Content-Type: ",
            CONNECTION_CLOSE = "Connection: close\r\n";
    public static final String
            TARGET_NAME = "{time}",
            PUBLIC = "public",
            CLASSIC_HTML = "/classic.html",
            INDEX_HTML = "/index.html",
            SPRING_SVG = "/spring.svg",
            SPRING_PNG = "/spring.png",
            RESOURCES_HTML = "/resources.html",
            STYLES_CSS = "/styles.css",
            APP_JS = "/app.js",
            LINKS_HTML = "/links.html",
            FORMS_HTML = "/forms.html",
            EVENTS_HTML = "/events.html",
            EVENTS_JS = "/events.js";
    public static final List<String> validPaths = List.of(INDEX_HTML, SPRING_SVG, SPRING_PNG, RESOURCES_HTML,
            STYLES_CSS, APP_JS, LINKS_HTML, FORMS_HTML, CLASSIC_HTML,
            EVENTS_HTML, EVENTS_JS);
    public static final int poolSize = 64;
    public static final ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);


    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                threadPool.submit(() -> connect(socket)); //на исполнение в пулл потоков
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void connect(Socket socket) {
        try (socket;
             final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {
            final var parts = getResponsePartsFromRequest(in);

            if (parts.length != PARTS_LENGTH) {
                // just close socket
                return;
            }

            final var path = parts[1];
            if (!validPaths.contains(path)) {
                not_found_404(out);
                return;
            }

            final var filePath = Path.of(".", PUBLIC, path);
            final var mimeType = Files.probeContentType(filePath);

            if (path.equals(CLASSIC_HTML)) {
                specialCaseForClassic(out, filePath, mimeType);
                return;
            }

            final var length = Files.size(filePath);
            outOk200(out, length, mimeType, filePath);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public void not_found_404(OutputStream out) {
        try {
            out.write((
                    NOT_FOUND_404 +
                            CONTENT_LENGTH_0 +
                            CONNECTION_CLOSE +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void specialCaseForClassic(OutputStream out, Path filePath, String mimeType) {
        final String template;
        try {
            template = Files.readString(filePath);

            final var content = template.replace(
                    TARGET_NAME,
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    OK_200 +
                            CONTENT_TYPE + mimeType + "\r\n" +
                            CONTENT_LENGTH + content.length + "\r\n" +
                            CONNECTION_CLOSE +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void outOk200(OutputStream out, long length, String mimeType, Path filePath) {
        try {
            out.write((
                    OK_200 +
                            CONTENT_TYPE + mimeType + "\r\n" +
                            CONTENT_LENGTH + length + "\r\n" +
                            CONNECTION_CLOSE +
                            "\r\n"
            ).getBytes());

            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String[] getResponsePartsFromRequest(BufferedReader in) {
        final String requestLine;
        try {
            requestLine = in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return requestLine.split(" ");
    }

}
