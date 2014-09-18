package org.gcastsamples.plotandcast;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.sample.castcompanionlibrary.cast.BaseCastManager;
import com.google.sample.castcompanionlibrary.cast.DataCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;

import static android.view.View.OnClickListener;


public class MainActivity extends ActionBarActivity implements OnClickListener{

    private DataCastManager mDataCastManager;

    private static final String TAG = MainActivity.class.getSimpleName();
    private EditText edtInitialValue;
    private EditText edtFinalValue;
    private EditText edtIncrement;
    private EditText edtFunction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BaseCastManager.checkGooglePlayServices(this);

        mDataCastManager = MyApplication.getDataCastManager(this);
        mDataCastManager.reconnectSessionIfPossible(this, true, 5 /*seconds*/);

        findViewById(R.id.btn_plot).setOnClickListener(this);
        findViewById(R.id.btn_example).setOnClickListener(this);

        edtInitialValue = (EditText) findViewById(R.id.edt_initial_value);
        edtFinalValue = (EditText) findViewById(R.id.edt_final_value);
        edtIncrement = (EditText) findViewById(R.id.edt_increment);
        edtFunction = (EditText) findViewById(R.id.edt_function);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataCastManager = MyApplication.getDataCastManager(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        mDataCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_example) {
            putExample();
        }
        if (view.getId() == R.id.btn_plot) {
            plotToCCast();
        }
    }

    private void putExample() {
        edtInitialValue.setText("-10.5");
        edtFinalValue.setText("10.5");
        edtIncrement.setText("0.3");
        edtFunction.setText("if (x >=0) return Math.sin(x); else return Math.cos(x);");
    }

    private void plotToCCast() {
        if (missingValues()) {
            Toast.makeText(this, R.string.missing_values, Toast.LENGTH_LONG).show();
            return;
        }

        String json = getData();
        try {
            mDataCastManager.sendDataMessage(json,
                    MyApplication.NAME_SPACE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(this, R.string.error_sending, Toast.LENGTH_LONG).show();
        }
    }

    private boolean missingValues() {
        return !Utils.allFilledWithText(edtInitialValue,
                edtFinalValue,
                edtIncrement,
                edtFunction);
    }

    private String getData() {
        return "{\"funct\" : \"" + edtFunction.getText() +  "\"," +
                "\"begin\" : " + edtInitialValue.getText() +  "," +
                "\"end\" : " + edtFinalValue.getText() + "," +
                "\"increment\" : " + edtIncrement.getText() + "}";
    }
}
