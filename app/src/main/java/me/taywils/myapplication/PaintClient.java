package me.taywils.myapplication;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URI;
import java.net.URL;

// JSON Reference http://www.tutorialspoint.com/json/json_java_example.htm

public class PaintClient {
    private final int BOARD_ROWS = 500;
    private final int BOARD_COLS = 500;
    private Socket socket;
    private boolean board[][] = new boolean[BOARD_ROWS][BOARD_COLS];

    public PaintClient() {
        initSocket();
    }

    public void initSocket() {
        try {
            final String serverUrlString = "http://warm-fortress-2906.herokuapp.com";

            URL serverUrl = new URL(serverUrlString);
            URI uri = new URI(
                    serverUrl.getProtocol(),
                    serverUrl.getHost(),
                    serverUrl.getQuery(),
                    null
            );

            socket = IO.socket(uri);

            /* BEGIN socket_event_handlers */
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    emitDebug();
                    Log.d("CONNECTED TO SERVER", "Sending debug message");
                }
            });

            socket.on("newClient", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("SERVER RESPONSE", args[0].toString());
                }
            });

            socket.on("debug", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("SERVER RESPONSE", args[0].toString());
                }
            });

            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("DISCONNECT", "Disconnected from server");
                }
            });
            /* END socket_event_handlers */

            socket.connect();
        } catch(Exception exception) {
            Log.e("EXCEPTION", exception.getMessage());
        }
    }

    private void initBoard() {
        for(int row = 0; row < BOARD_ROWS; ++row) {
            for(int col = 0; col < BOARD_COLS; ++col) {
                board[row][col] = false;
            }
        }
    }

    public void emitPaint(int x, int y) {
        Log.i("PAINTVIEW", "emitPaint()");
        try {
            board[x][y] = true;

            JSONObject paintCoord = new JSONObject();

            paintCoord.put("x", x);
            paintCoord.put("y", y);

            socket.emit("paint", paintCoord);
        } catch(Exception exception) {
            Log.e("EXCEPTION", exception.getMessage());
        }
    }

    public void emitClear() {
        Log.i("PAINTVIEW", "emitClear()");
        initBoard();
        socket.emit("clear", "clear");
    }

    public void emitDebug() {
        Log.i("PAINTVIEW", "emitDebug()");
        socket.emit("debug", "Hello from Android PaintClient");
    }

    public void disconnectSocket() {
        Log.i("PAINTVIEW", "disconnectSocket()");
        socket.disconnect();
    }
}
