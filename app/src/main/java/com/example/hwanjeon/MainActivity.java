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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.*;


public class MainActivity extends AppCompatActivity {

    static RequestQueue requestQueue;
    Button button;
    TextView textView;
    Money money;
    int i = 0;
    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Double> exchangeRates;

    String selectedCountry = "USD";

    String[] countries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);


        sendRequest();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttondialog();
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
                        println_b(selectedCountry);
                        println(exchangeRates.get(selectedCountry).toString());
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
                        println("확인");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        println("에러 -> " + error.getMessage());
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





    public void println(String data) {
        textView.setText(data +"\n");
    }

    public void println_b(String data) {
        button.setText(data +"\n");
    }
}