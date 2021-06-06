package sample;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Application {

    static public class MyStompSessionHandler
            extends StompSessionHandlerAdapter {
        private final String userId;

        public MyStompSessionHandler(String userId) {
            this.userId = userId;
        }

        private void showHeaders(StompHeaders headers) {
            for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                System.err.print("  " + e.getKey() + ": ");
                boolean first = true;
                for (String v : e.getValue()) {
                    if (!first) System.err.print(", ");
                    System.err.print(v);
                    first = false;
                }
                System.err.println();
            }
        }


        private void subscribeTopic(String topic, StompSession session) {
            session.subscribe(topic, new StompFrameHandler() {

                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return ServerMessage.class;
                }

                @Override
                public void handleFrame(StompHeaders headers,
                                        Object payload) {
                    //showHeaders(headers);

                    System.err.println(payload.toString());
                }
            });
        }

        @Override
        public void afterConnected(StompSession session,
                                   StompHeaders connectedHeaders) {
            System.err.println("Connected!");
            //showHeaders(connectedHeaders);

            subscribeTopic("/topic/messages", session);
            subscribeTopic("/topic/users/" + session.getSessionId(), session);
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            super.handleFrame(headers, payload);
            System.err.println(headers.get("message"));
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            //exception.printStackTrace();
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            //exception.printStackTrace();
        }
    }

    private static StompSession autorize(BufferedReader in, ArrayList<String> user) {
        WebSocketClient simpleWebSocketClient =
                new StandardWebSocketClient();

        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(simpleWebSocketClient));


        SockJsClient sockJsClient = new SockJsClient(transports);
        String login = "";
        String pass = "";
        try {
            System.out.print("Login:");
            login = in.readLine();
            System.out.print("Password:");
            pass = in.readLine();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        StompHeaders connectHeaders = new StompHeaders();
        if (login.charAt(0) == '/') {
            login = login.substring(1);
            connectHeaders.add("register", "true");
        }
        connectHeaders.add("login", login);
        connectHeaders.add("pass", pass);
        user.add(login);

        WebSocketStompClient stompClient =
                new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String url = "ws://localhost:8080/chat";

        StompSessionHandler sessionHandler = new MyStompSessionHandler(login);

        try {
            return stompClient.connect(url, new WebSocketHttpHeaders(), connectHeaders, sessionHandler).get();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        ArrayList<String> user = new ArrayList<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StompSession session = autorize(in, user);
        for (; ; ) {
            try {
                session.isConnected();
            } catch (NullPointerException e) {
                session = autorize(in, user);
            }

            System.out.flush();
            String line = in.readLine();
            if (line == null) break;
            if (line.length() == 0) continue;
            if (line.charAt(0) == '/') {
                session.send("/app/chat/users/" + session.getSessionId(), null);
            } else {
                ClientMessage msg = new ClientMessage(user.get(0), line);
                session.send("/app/chat/" + session.getSessionId(), msg);
            }
        }
    }
}
