package me.taywils.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.UUID;

public class MyActivity extends Activity {
    private PaintView paintView;
    private PaintClient paintClient;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        this.paintView = (PaintView)findViewById(R.id.activity_my_view_whiteboard);
        this.testSocketIoClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        paintClient.disconnectSocket();
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
        paintClient.emitClear();
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
            paintClient = new PaintClient();
            paintClient.setPaintView(paintView);
            paintClient.setContext(this);
        } catch(Exception exception) {
            Log.e("EXCEPTION", exception.getMessage());
        }
    }
}
