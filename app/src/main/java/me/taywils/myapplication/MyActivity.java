package me.taywils.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URI;
import java.net.URL;
import java.util.UUID;

public class MyActivity extends Activity {
    private PaintView paintView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        this.testSocketIoClient();
        this.paintView = (PaintView)findViewById(R.id.activity_my_view_whiteboard);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void buttonClearClick(View view) {
        Log.i("INFO", "Clear button clicked");

        paintView.clearCanvas();
    }

    public void buttonSnapshotClick(View view) {
        Log.i("INFO", "Snapshot button clicked");

        paintView.setDrawingCacheEnabled(true);

        String saved = MediaStore.Images.Media.insertImage(
            getContentResolver(),
            paintView.getDrawingCache(),
            UUID.randomUUID().toString() + ".png",
            "drawing"
        );

        Toast saveToast;

        if(null != saved) {
            saveToast = Toast.makeText(
                getApplicationContext(),
                "Whiteboard snapshot saved!",
                Toast.LENGTH_SHORT
            );

        } else {
            saveToast = Toast.makeText(
                getApplicationContext(),
                "Whiteboard snapshot failed!",
                Toast.LENGTH_SHORT
            );
        }

        saveToast.show();

        paintView.destroyDrawingCache();
    }

    protected void testSocketIoClient() {
        try {
            String serverUrlString = "http://warm-fortress-2906.herokuapp.com";
            URL serverUrl = new URL(serverUrlString);
            URI uri = new URI(
                    serverUrl.getProtocol(),
                    serverUrl.getHost(),
                    serverUrl.getQuery(),
                    null
            );
            final Socket socket = IO.socket(uri);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socket.emit("debug", "Hello from Android");
                    Log.d("SENT REQ TO SERVER", "Hello from Android");
                }
            }).on("debug", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("SERVER RESPONSE", args[0].toString());
                    socket.disconnect();
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("DISCONNECT", "Disconnected from server");
                }
            });

            socket.connect();
        } catch(Exception exception) {
            Log.e("EXCEPTION", exception.getMessage());
        }
    }
}
