package com.project.semicolon.voiceinteraction;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, RecognitionListener, View.OnClickListener {
    private static final int REQ_CODE_SPEECH_INPUT = 1000;
    private static final String TAG = "MainActivity";
    private TextView mTxtCommand;
    private FloatingActionButton fab;
    private PermissionHelper permissionHelper;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private String recordAudioPermission = Manifest.permission.RECORD_AUDIO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTxtCommand = findViewById(R.id.txt_say_something);
        fab = findViewById(R.id.btn_speak);

        permissionHelper = new PermissionHelper(this);
        initTextSpeech();
        initSpeechRecognizer();

        fab.setOnClickListener(this);

    }

    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(this);
        }
    }

    private void initTextSpeech() {
        textToSpeech = new TextToSpeech(this, this);
    }

    private void inputSpeech() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d(TAG, "onActivityResult: " + results);
                    mTxtCommand.setText(results.get(0));
                }

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onInit(int status) {
        if (textToSpeech.getEngines().size() == 0) {
            Toast.makeText(this, "No Engine", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textToSpeech.setLanguage(Locale.US);
        speak("Hello, there.");
    }


    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.d(TAG, "onReadyForSpeech: ");


    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech: ");

    }

    @Override
    public void onRmsChanged(float v) {
        Log.d(TAG, "onRmsChanged() returned: " + v);

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {
        Log.e(TAG, "onError: ");


    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d(TAG, "onResults: " + result.toString());
        process(result.get(0));

    }

    private void process(String result) {
        String message = result.trim().toLowerCase();

        if (message.contains("open")) {
            if (message.contains("whatsapp")) {
                openApp(AppPackages.PACKAGE_WHATSAPP);

            } else if (message.contains("calculator")) {
                openApp(AppPackages.PACKAGE_CALCULATOR);
            } else if (message.contains("google chrome")) {
                openApp(AppPackages.PACKAGE_BROWSER);

            }
        } else if (message.contains("close")) {
            if (message.contains("app")) {
                this.finishAffinity();
                finishAndRemoveTask();
            }


        } else if (message.contains("remind me")) {
            Log.d(TAG, "process: " + message);
            String[] strSplit = message.split(" ");
            String reminder = strSplit[3];

            String[] split = strSplit[5].split("-");
            int min = Integer.parseInt(split[0]);

            setReminder(reminder, min);
            mTxtCommand.setText(String.format(Locale.ENGLISH,
                    "Remind me of %s every %d min(s)", reminder, min));


        }
    }


    private void setReminder(String reminder, int min) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.add(Calendar.SECOND, min * 60);


        manager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                1000 * 60 * min,
                pendingIntent);

    }

    private void openApp(String packageName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        startActivity(launchIntent);
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    private void speak(String userMessage) {
        if (Build.VERSION.SDK_INT >= 21) {
            textToSpeech.speak(userMessage, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(userMessage, TextToSpeech.QUEUE_FLUSH, null);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        textToSpeech.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSpeechRecognizer();
        initTextSpeech();

    }

    @Override
    public void onClick(View view) {
        if (permissionHelper.checkDevice()) {
            if (!permissionHelper.isPermissionGranted(recordAudioPermission)) {
                permissionHelper.requestPermission(MainActivity.this,
                        recordAudioPermission, REQ_CODE_SPEECH_INPUT);
                return;

            }

            startSpeechRecognizer();


        }

    }

    private void startSpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizer.startListening(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognizer();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
