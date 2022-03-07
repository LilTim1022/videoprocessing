package com.example.videoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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
    float[] history = new float[2]; // Declared history Array to store movement value
    String[] direction = {"NONE", "NONE"}; // Declared directions Array
    Button overlayBtn;
    Button playBtn;
    Button selectVidBtn;
    Button uploadBtn;
    Button upload_pageBtn;
    SensorManager manager; // Declared a sensor manager
    Sensor accelerometer; // Declared a Sensor
    String apiBaseUrl = "http://10.255.118.21:5002/"; // sample REST api base url where Python flask API run


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // play video when app run
        playVideo("getVideo");
        playBtn = (Button) findViewById(R.id.playBtn);
        overlayBtn = (Button) findViewById(R.id.overlayBtn);
        selectVidBtn = (Button) findViewById(R.id.selectVidBtn);
        uploadBtn = (Button) findViewById(R.id.uploadBtn);
        upload_pageBtn = (Button) findViewById(R.id.to_uploadpage_Btn);
        // add a flag as tag in overlay button to avoid initial API call for sensor event change
        overlayBtn.setTag("m");

        //通过AsyncHttpClient获得播放视频的Url
        asyncHttpClient = new AsyncHttpClient();
        overlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //disable overlay button after click
                    overlayBtn.setClickable(false);
                    // change overlay button test after click
                    overlayBtn.setText("Processing..");
                    // change the flag as tag in overlay button to API call for sensor event change
                    overlayBtn.setTag("d");
                    StringEntity entity = new StringEntity("");

                    // A REST API POST call to insert an overlay inside the video
                    asyncHttpClient.post(v.getContext(), apiBaseUrl + "insertOverlay", entity, "application/json",
                            new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    try {
                                        // enable overlay button after API response
                                        overlayBtn.setClickable(true);
                                        overlayBtn.setText("Insert Overlay");
                                        String videoUrl = response.getString("overlayUrl");
                                        playVideo(videoUrl);
                                    } catch (JSONException e) {
                                        // TOAST to show the message to the user
                                        Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
                                        // enable overlay button after API response
                                        overlayBtn.setClickable(true);
                                        overlayBtn.setText("Insert Overlay");
                                        overlayBtn.setTag("m");
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                                    // enable overlay button after API response
                                    overlayBtn.setClickable(true);
                                    overlayBtn.setText("Insert Overlay");
                                    overlayBtn.setTag("m");
                                    // TOAST to show the message to the user
                                    Toast.makeText(getApplicationContext(), "Failed to process API data.", Toast.LENGTH_LONG).show();
                                }
                            });
                } catch (Exception e) {
                    // TOAST to show the message to the user
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        //播放按钮
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //disable overlay button after click
                    playBtn.setClickable(false);
                    // change overlay button test after click
                    playBtn.setText("FindingVideo..");
                    // change the flag as tag in overlay button to API call for sensor event change
                    playBtn.setTag("d");
                    StringEntity entity = new StringEntity("");

                    // A REST API POST call to insert an overlay inside the video
                   playVideo("getOverlayVideo");
                   playBtn.setText("Play Overlay Video");
                } catch (Exception e) {
                    // TOAST to show the message to the user
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

         uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //disable overlay button after click
//                    playBtn.setClickable(false);
                    // change overlay button test after click
//                    playBtn.setText("FindingVideo..");
                    // change the flag as tag in overlay button to API call for sensor event change
//                    playBtn.setTag("d");
//                    StringEntity entity = new StringEntity("");

                    // A REST API POST call to insert an overlay inside the video
//                   playVideo("getOverlayVideo");
//                   playBtn.setText("Play Overlay Video");
                     Intent intent=new Intent(MainActivity.this,VideoUploadActivity.class);
                     startActivity(intent);
                } catch (Exception e) {
                    // TOAST to show the message to the user
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
        // execute when Sensor Event changes

        float xChange = history[0] - event.values[0];
        float yChange = history[1] - event.values[1];

        // store initial X and Y axis value of sensor
        history[0] = event.values[0];
        history[1] = event.values[1];

        // store initial direction of sensor
        String directionX = direction[0];
        String directionY = direction[1];

        // detect direction change based on X axis changes
        if (xChange > 2) {
            direction[0] = "LEFT";
        } else if (xChange < -2) {
            direction[0] = "RIGHT";
        }

        // detect direction change based on Y axis changes
        if (yChange > 2) {
            direction[1] = "DOWN";
        } else if (yChange < -2) {
            direction[1] = "UP";
        }

        // do if direction change in X axis after overlay added
        if (!directionX.equals(direction[0]) && overlayBtn.getTag().equals("d")) {
            try {
                // TOAST to show the message to the user
                Toast.makeText(getApplicationContext(), "Moving overlay wait please.", Toast.LENGTH_SHORT).show();
                StringEntity entity = new StringEntity("");
                String url = apiBaseUrl + "updateOverlay/" + event.values[0] + "/" + event.values[1];

                // A REST API PUT call to move the inserted overlay inside the video
                asyncHttpClient.put(context, url, entity, "application/json",
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    // TOAST to show the message to the user
                                    Toast.makeText(getApplicationContext(), "Overlay moved.", Toast.LENGTH_SHORT).show();
                                    String videoUrl = response.getString("overlayUrl");
                                    playVideo(videoUrl);
                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "" + e, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                                // TOAST to show the message to the user
                                Toast.makeText(getApplicationContext(), "Failed to process API data.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (Exception e) {
                // TOAST to show the message to the user
                Toast.makeText(getApplicationContext(), "Exception:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        // do if direction change in Y axis after overlay added
        if (!directionY.equals(direction[1]) && overlayBtn.getTag().equals("d")) {

            try {
                // TOAST to show the message to the user
                Toast.makeText(getApplicationContext(), "Moving overlay wait please.", Toast.LENGTH_SHORT).show();
                StringEntity entity = new StringEntity("");
                String url = apiBaseUrl + "updateOverlay/" + event.values[0] + "/" + event.values[1];

                // A REST API PUT call to move the inserted overlay inside the video
                asyncHttpClient.put(context, url, entity, "application/json",
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    // TOAST to show the message to the user
                                    Toast.makeText(getApplicationContext(), "Overlay moved.", Toast.LENGTH_SHORT).show();
                                    String videoUrl = response.getString("overlayUrl");
                                    playVideo(videoUrl);
                                } catch (JSONException e) {
                                    // TOAST to show the message to the user
                                    Toast.makeText(getApplicationContext(), "" + e, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                                // TOAST to show the message to the user
                                Toast.makeText(getApplicationContext(), "Failed to process API data.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (Exception e) {
                // TOAST to show the message to the user
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
