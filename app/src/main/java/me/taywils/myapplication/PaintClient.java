package me.taywils.myapplication;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URL;

// JSON Reference http://www.tutorialspoint.com/json/json_java_example.htm

public class PaintClient {
    private final int BOARD_ROWS = 500;
    private final int BOARD_COLS = 500;
    private Socket socket;
    private boolean board[][] = new boolean[BOARD_ROWS][BOARD_COLS];
    private PaintView paintView;
    private Context context;

    public PaintClient() {
        initSocket();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setPaintView(PaintView paintView) {
        this.paintView = paintView;
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
                    Log.d("onConnect", "Sending debug message");
                }
            });

            socket.on("debug", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("onDebug", args[0].toString());
                }
            });

            socket.on("clear", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("onClear", args[0].toString());
                    Handler mainHandler = new Handler(context.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            paintView.clearCanvas();
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            });

            socket.on("newClient", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        Log.d("onNewClient", "PaintClient onNewClient");
                        final JSONArray jsonBoard = (JSONArray) args[0];

                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    for (int row = 0; row < BOARD_ROWS; ++row) {
                                        for (int col = 0; col < BOARD_COLS; ++col) {
                                            JSONArray jsonBoardRow = (JSONArray) jsonBoard.get(row);
                                            Boolean boolVal = (Boolean) jsonBoardRow.get(col);
                                            if(boolVal) {
                                                paintView.paintPoint(row, col);
                                            }
                                        }
                                    }
                                } catch(Exception exception) {
                                    Log.e("EXCEPTION", exception.getMessage());
                                }
                            }
                        };
                        mainHandler.post(myRunnable);
                    } catch(Exception exception) {
                        Log.e("EXCEPTION", exception.getMessage());
                    }
                }
            });

            socket.on("paint", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        Log.d("onPaint", args[0].toString());

                        JSONObject paintCoordObj = (JSONObject) args[0];
                        final Integer x = paintCoordObj.getInt("x");
                        final Integer y = paintCoordObj.getInt("y");

                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                paintView.paintPoint(x, y);
                            }
                        };
                        mainHandler.post(myRunnable);
                    } catch(Exception exception) {
                        Log.e("EXCEPTION", exception.getMessage());
                    }
                }
            });

            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("onDisconnect", "Disconnected from server");
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
