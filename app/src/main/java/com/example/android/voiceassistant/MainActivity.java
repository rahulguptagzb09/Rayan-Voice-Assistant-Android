package com.example.android.voiceassistant;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    static final int REQUEST_PERMISSION_KEY = 1;
    ImageButton recordbtn;
    private TextView returnedText;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        returnedText = findViewById(R.id.textView1);
        progressBar = findViewById(R.id.progressBar1);
        recordbtn = findViewById(R.id.mainButton);

        String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO};
        if (!Function.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }

        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        /*
        Minimum time to listen in millis. Here 5 seconds
         */
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        recognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);

        recordbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                progressBar.setVisibility(View.VISIBLE);
                speech.startListening(recognizerIntent);
                recordbtn.setEnabled(false);
            }
        });
    }

    private void inputFunction() {
        String input = returnedText.getText().toString().toLowerCase();

        if (input.contains("alarm")) {
            int i = extractInt(input);
            int min = i % 100;
            int hr = i / 100;
            createAlarm(hr, min, extractWordFromString(input, "message"));

        } else if (input.contains("timer")) {
            int i = extractInt(input);
            startTimer(i, extractWordFromString(input, "message"));

        } else if (input.contains("calender")) {
            int i = extractInt(input);
            int end = i % 100;
            int begin = i / 100;
            addEvent(extractWordFromString(input, "title"), extractWordFromString(input, "location"), begin * 3600, end * 3600);

        } else if (input.contains("image") || input.contains("camera") || input.contains("photo")) {
            capturePhoto();

        } else if (input.contains("video")) {
            captureVideo();

        } else if (input.contains("contact")) {
            insertContact(extractWordFromString(input, "name"), extractEmailFromString(input));

        } else if (input.contains("mail")) {
            composeEmail(extractEmailFromString(input), extractWordFromString(input, "subject"));

        } else if (input.contains("music") || input.contains("play")) {
            playSearchArtist(extractWordFromString(input, "artist"));

        } else if (input.contains("call")) {
            dialPhoneNumber(extractPhoneNumbersFromString(input));

        } else if (input.contains("web")) {
            searchWeb(extractWordFromString(input, "search"));

        } else if (input.contains("wi-fi")) {
            openWifiSettings();

        } else if (input.contains("youtube")) {
            searchYoutube(extractWordFromString(input, "youtube"));

        } else if (input.contains("maps")) {
            locationSearchMaps(extractWordFromString(input, "maps"));

        } else if (input.contains("navigation")) {
            navigationSearcMaps(extractWordFromString(input, "navigation"));

        } else if (input.contains("chrome")) {
            searchChrome(extractWordFromString(input, "chrome"));
        }
        //else if (input.contains("chrome")) {
        //  chrome("https://www.apple.com/in/");
        //} else if (input.contains("browser")) {
        //openWebPage("https://www.apple.com/in/");
        //}
    }

    private int extractInt(String input) {
        StringBuilder result = new StringBuilder();
        int a = 0;
        for (int i = 0; i < input.length(); i++) {
            Character chars = input.charAt(i);
            if (Character.isDigit(chars)) {
                result.append(chars);
            }
        }
        if (result.length() > 0) {
            a = Integer.parseInt(result.toString());
        }
        return a;
    }

    public String extractWordFromString(String input, String match) {
        String word = "";
        Scanner s = new Scanner(input);
        while (s.hasNext()) {
            if (s.next().equals(match)) {
                word = s.next();
            }
        }
        return word;
    }

    public String extractEmailFromString(String input) {
        String email = "";
        Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(input);
        while (m.find()) {
            email = m.group();
        }
        return email;
    }

    public String extractPhoneNumbersFromString(String input) {
        String num = "";
        Pattern pattern = Pattern.compile("\\d{4} \\d{3} \\d{3}");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            num = matcher.group(0);
        }
        return num;
    }

    public void createAlarm(int hour, int minutes, String message) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        if (message != null) {
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);
        }
        if (hour > 0) {
            intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        }
        if (minutes > 0) {
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void startTimer(int seconds, String message) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER);
        if (message != null) {
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);
        }
        if (seconds > 0) {
            intent.putExtra(AlarmClock.EXTRA_LENGTH, seconds);
        }
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void addEvent(String title, String location, long begin, long end) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        if (title != null) {
            intent.putExtra(CalendarContract.Events.TITLE, title);
        }
        if (location != null) {
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
        }
        if (begin > 0) {
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin);
        }
        if (end > 0) {
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end);
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void capturePhoto() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void captureVideo() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void insertContact(String name, String email) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (name != null) {
            intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        }
        if (email != null) {
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void composeEmail(String address, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        if (address != null) {
            intent.putExtra(Intent.EXTRA_EMAIL, address);
        }
        if (subject != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void playSearchArtist(String artist) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS,
                MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
        if (artist != null) {
            intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist);
            intent.putExtra(SearchManager.QUERY, artist);
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        if (phoneNumber != null) {
            intent.setData(Uri.parse("tel:" + phoneNumber));
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void searchWeb(String query) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        if (query != null) {
            intent.putExtra(SearchManager.QUERY, query);
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void openWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void searchYoutube(String query) {
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setPackage("com.google.android.youtube");
        intent.putExtra("query", query);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void locationSearchMaps(String query) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + query);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    public void navigationSearcMaps(String query) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + query);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    public void searchChrome(String query) {
        Uri uri = Uri.parse("http://www.google.com/#q=" + query);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

   /* public void chrome(String url) {
        try {
            Intent i = new Intent("android.intent.action.MAIN");
            i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
            i.addCategory("android.intent.category.LAUNCHER");
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            // Chrome is not installed
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        }
    }*/

    /*public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }*/

    @Override
    public void onBeginningOfSpeech() {
        Log.d("Log", "onBeginningOfSpeech");
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.d("Log", "onPartialResults");
        ArrayList<String> matches = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text;
        text = matches.get(0).toLowerCase();
        returnedText.setText(text);
    }

    @Override
    public void onEndOfSpeech() {
        Log.d("Log", "onEndOfSpeech");
        progressBar.setVisibility(View.INVISIBLE);
        recordbtn.setEnabled(true);
        inputFunction();
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d("Log", "onBufferReceived: " + Arrays.toString(buffer));
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d("Log", "FAILED " + errorMessage);
        progressBar.setVisibility(View.INVISIBLE);
        returnedText.setText(errorMessage);
        recordbtn.setEnabled(true);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.d("Log", "onEvent");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.d("Log", "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.d("Log", "onResults");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.d("Log", "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_type) {
            Intent intent = new Intent(this, TypeActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

