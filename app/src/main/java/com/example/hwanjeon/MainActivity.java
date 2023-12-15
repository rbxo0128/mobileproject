package com.example.hwanjeon;

import static java.sql.DriverManager.println;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.*;


public class MainActivity extends AppCompatActivity {

    static RequestQueue requestQueue;
    Button country1, country2,swap;
    TextView textView,change;
    EditText editText;
    Money money;
    int i = 0;
    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Double> exchangeRates;

    String selectedCountry = "KRW";
    String selectedCountry2 = "USD";

    String[] countries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        country1 = (Button) findViewById(R.id.country1);
        country2 = (Button) findViewById(R.id.country2);
        swap = (Button) findViewById(R.id.swap);
        textView = (TextView) findViewById(R.id.textView);
        change = (TextView) findViewById(R.id.change);
        editText = (EditText) findViewById(R.id.editTextNumber);


        sendRequest();
        country1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttondialog();
            }
        });

        country2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttondialog2();
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // 텍스트 변경 전에 수행할 작업
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // 텍스트가 변경될 때마다 수행할 작업
                //updateTextView(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    println_change(String.valueOf(String.format("%.3f", exchangeRates.get(selectedCountry2) / exchangeRates.get(selectedCountry) * Integer.parseInt(editText.getText().toString()))));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    println_change("에러 (입력 값 변경)");
                }
            }
        });


    }

    private void buttondialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("오늘의 환전")
                .setItems(countries, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCountry = countries[which];
                        println_con1(selectedCountry);
                        try {
                            println_change(String.valueOf(String.format("%.3f", exchangeRates.get(selectedCountry2) / exchangeRates.get(selectedCountry) * Integer.parseInt(editText.getText().toString()))));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            println_change("에러 (입력 값 변경)");
                        }


                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void buttondialog2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("오늘의 환전")
                .setItems(countries, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCountry2 = countries[which];
                        println_con2(selectedCountry2);
                        try {
                            println_change(String.valueOf(String.format("%.3f", exchangeRates.get(selectedCountry2) / exchangeRates.get(selectedCountry) * Integer.parseInt(editText.getText().toString()))));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            println_change("에러 (입력 값 변경)");
                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void sendRequest() {
        String url = "https://v6.exchangerate-api.com/v6/732d487587b46ef13da1b493/latest/USD";
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String country = parseResponse(response);
                        exchangeRates = parseJson(country);
                        countries = exchangeRates.keySet().toArray(new String[0]);
                        try {
                            println_change(String.valueOf(String.format("%.3f", exchangeRates.get(selectedCountry2) / exchangeRates.get(selectedCountry) * Integer.parseInt(editText.getText().toString()))));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            println_change("에러 (입력 값 변경)");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return new HashMap<>();
            }
        };

        request.setShouldCache(false);
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
        println("요청 보냄.");
    }

    private String parseResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

            // "conversion_rates" 값 추출
            JsonNode conversionRatesNode = jsonNode.get("conversion_rates");

            String result = conversionRatesNode.toString();

            return result;
        } catch (IOException e) {
            Log.e("YourActivity", "Error parsing JSON", e);
            return "fail";
        }
    }

    private static Map<String, Double> parseJson(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Double> exchangeRates = new HashMap<>();

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            // JSON 노드를 순회하며 나라와 환율을 맵에 저장
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String country = entry.getKey();

                Double rate = entry.getValue().asDouble();
                exchangeRates.put(country, rate);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return exchangeRates;
    }





    public void println_text(String data) {
        textView.setText(data +"\n");
    }
    public void println_change(String data) {
        change.setText(data +"\n");
    }

    public void println_con1(String data) {
        country1.setText(data);
    }
    public void println_con2(String data) {
        country2.setText(data);
    }
}