package org.example;

import java.io.BufferedOutputStream;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();
        server.listen(9999);
        // код инициализации сервера (из вашего предыдущего ДЗ)

        // добавление хендлеров (обработчиков)
//        server.addHandler("GET", "/messages", new Handler() {
//            public void handle(Request request, BufferedOutputStream responseStream) {
//                // TODO: handlers code
//            }
//        });
//        server.addHandler("POST", "/messages", new Handler() {
//            public void handle(Request request, BufferedOutputStream responseStream) {
//                // TODO: handlers code
//            }
//        });


    }
}