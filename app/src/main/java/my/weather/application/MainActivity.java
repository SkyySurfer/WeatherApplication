package my.weather.application;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import weather.forecast.library.GettingWeather;
import weather.forecast.library.UpdateUIListener;

public class MainActivity extends AppCompatActivity implements UpdateUIListener {

    Button ok;
    EditText field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = field.getText().toString();

                hideKeyBoard();

                if (location.length()>0){
                    GettingWeather gettingWeather = new GettingWeather();
                    gettingWeather.getCurrentWeather(MainActivity.this, location);

                }
                else{
                    Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void hideKeyBoard(){
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void findViews(){
        ok = findViewById(R.id.ok);
        field = findViewById(R.id.field);
    }

    public void showDialog(String text) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View dialogView = layoutInflater.inflate(R.layout.result_window, null);

        final AlertDialog alertD = new AlertDialog.Builder(this).setCancelable(false).create();

        TextView textView = dialogView.findViewById(R.id.text);
        Button ok = dialogView.findViewById(R.id.ok);


        textView.setText(text);

        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                alertD.cancel();
            }
        });

        alertD.setView(dialogView);
        alertD.show();
    }

    @Override
    public void onDataAvailable(String res) {
        showDialog(res);
    }

        @Override
    public void onBackPressed() {

    }


}
