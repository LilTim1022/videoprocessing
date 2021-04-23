package com.example.videoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.MediaController;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    AsyncHttpClient asyncHttpClient;
    Context context;
    float[] history = new float[2];
    String[] direction = {"NONE", "NONE"};
    Button overlayBtn;
    SensorManager manager; // Declared a sensor manager
    Sensor accelerometer; // Declared a Sensor
    String apiBaseUrl = "http://192.168.2.18:5002/"; // sample REST api base url where Python flask API run


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playVideo("getVideo");
        overlayBtn = (Button) findViewById(R.id.overlayBtn);
        overlayBtn.setTag("m");

        asyncHttpClient = new AsyncHttpClient();
        overlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    overlayBtn.setClickable(false);
                    overlayBtn.setText("Processing..");
                    overlayBtn.setTag("d");
                    StringEntity entity = new StringEntity("");

                    // A REST API POST call to insert an overlay inside the video
                    asyncHttpClient.post(v.getContext(), apiBaseUrl + "insertOverlay", entity, "application/json",
                            new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    try {
                                        overlayBtn.setClickable(true);
                                        overlayBtn.setText("Insert Overlay");
                                        String videoUrl = response.getString("overlayUrl");
                                        playVideo(videoUrl);
                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
                                        overlayBtn.setClickable(true);
                                        overlayBtn.setText("Insert Overlay");
                                        overlayBtn.setTag("m");
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                                    overlayBtn.setClickable(true);
                                    overlayBtn.setText("Insert Overlay");
                                    overlayBtn.setTag("m");
                                    Toast.makeText(getApplicationContext(), "Failed to process API data.", Toast.LENGTH_LONG).show();
                                }
                            });
                } catch (Exception e) {
                    System.out.println("Exception:" + e.getMessage());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        // An instance of the sensor service, and use that to get an instance of a particular sensor.
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        // Unregister the sensor when the activity pauses.
        super.onPause();
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float xChange = history[0] - event.values[0];
        float yChange = history[1] - event.values[1];

        history[0] = event.values[0];
        history[1] = event.values[1];
        String directionX = direction[0];
        String directionY = direction[1];

        if (xChange > 2) {
            direction[0] = "LEFT";
        } else if (xChange < -2) {
            direction[0] = "RIGHT";
        }

        if (yChange > 2) {
            direction[1] = "DOWN";
        } else if (yChange < -2) {
            direction[1] = "UP";
        }

        if (!directionX.equals(direction[0]) && overlayBtn.getTag().equals("d")) {
            try {

                Toast.makeText(getApplicationContext(), "Moving overlay wait please.", Toast.LENGTH_SHORT).show();
                StringEntity entity = new StringEntity("");
                String url = apiBaseUrl + "updateOverlay/" + event.values[0] + "/" + event.values[1];

                // A REST API PUT call to move the inserted overlay inside the video
                asyncHttpClient.put(context, url, entity, "application/json",
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    Toast.makeText(getApplicationContext(), "Overlay moved.", Toast.LENGTH_SHORT).show();
                                    System.out.println(".............:" + response);
                                    String videoUrl = response.getString("overlayUrl");
                                    playVideo(videoUrl);
                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "" + e, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                                System.out.println("Failed.............:" + response);
                                Toast.makeText(getApplicationContext(), "Failed to process API data.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (Exception e) {
                System.out.println("Exception:" + e.getMessage());
                Toast.makeText(getApplicationContext(), "Exception:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (!directionY.equals(direction[1]) && overlayBtn.getTag().equals("d")) {
            Toast.makeText(getApplicationContext(), "Moving overlay wait please.", Toast.LENGTH_SHORT).show();
            try {
                StringEntity entity = new StringEntity("");
                String url = apiBaseUrl + "updateOverlay/" + event.values[0] + "/" + event.values[1];

                // A REST API PUT call to move the inserted overlay inside the video
                asyncHttpClient.put(context, url, entity, "application/json",
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    Toast.makeText(getApplicationContext(), "Overlay moved.", Toast.LENGTH_SHORT).show();
                                    String videoUrl = response.getString("overlayUrl");
                                    playVideo(videoUrl);
                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "" + e, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                                System.out.println("Failed.............:" + response);
                                Toast.makeText(getApplicationContext(), "Failed to process API data.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (Exception e) {
                System.out.println("Exception:" + e.getMessage());
                Toast.makeText(getApplicationContext(), "Exception:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // method to play the video
    protected void playVideo(String videoUrl) {
        VideoView videoView = (VideoView) findViewById(R.id.vdVw);
        videoView.setVideoPath(apiBaseUrl + videoUrl);
        MediaController mediaController = new
                MediaController(MainActivity.this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.start();
    }
}
