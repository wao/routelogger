package info.thinkmore.android.routelogger;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.val;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.menu_main)
public class MainActivity extends AppCompatActivity {
    final static String TAG = "RouteLogger";

    @ViewById(R.id.tv_console)
    TextView console;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    Handler handler = new Handler();

    @AfterViews
    protected void afterViews() {
        //TODO: Disabled in production release
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
    .detectAll().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
    .penaltyLog().build());
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        RouteLoggerService_.intent(getApplication()).start();

        console.setMovementMethod( new ScrollingMovementMethod());
    }

    @Background
    void loadLogFile() {
        try{
            try( val reader = new BufferedReader( new InputStreamReader(openFileInput("locations.txt") ) ) ) {
                val buf = new StringBuilder();
                while (reader.ready()) {
                    buf.append(reader.readLine());
                    buf.append("\n");
                }

                handler.post(() -> console.setText(buf.toString()));
            }
        }catch(FileNotFoundException ex){
            Log.v(TAG, "locations.txt hasn't been created yet.");
        }catch(IOException ex){
            //close() throw exception and we can't do anything just report it.
            //readLine() throws exception just report it.
            throw new RuntimeException(ex);
        }

    }


    @OptionsItem(R.id.action_reload)
    void reloadLogFile(){
        loadLogFile();
    }

    @Override
    public void onStart(){
        super.onStart();

        loadLogFile();
    }
}
