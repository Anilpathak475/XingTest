package uk.me.jeffsutton.xingchallenge;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Main entry point for App.
 * <p/>
 * Container Activity holding MainActivityFragment
 */
public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
