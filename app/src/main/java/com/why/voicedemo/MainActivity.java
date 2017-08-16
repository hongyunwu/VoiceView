package com.why.voicedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.autoio.voice_view.VoiceView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final VoiceView voiceView = (VoiceView) findViewById(R.id.voice_view);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        voiceView.changeState(VoiceView.PRE_SEARCH_STATE);
                    }
                });

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(11000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        voiceView.changeState(VoiceView.END_SEARCH_STATE);
                    }
                });

            }
        }).start();

    }
}
