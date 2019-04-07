package com.example.android.voiceassistant;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeActivity extends AppCompatActivity{

    private EditText inputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        inputTextView = findViewById(R.id.input);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputFunction();
            }
        });
    }

    private void inputFunction() {
        String input = inputTextView.getText().toString().toLowerCase();

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
        Pattern pattern = Pattern.compile("\\d{4}\\d{3}\\d{3}");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_type) {
            Intent intent = new Intent(this,TypeActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_about) {
            Intent intent = new Intent(this,HelpActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}