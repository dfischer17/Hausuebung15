package com.example.mastermind;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // Initialvariablen
    String[] alpahabet = null;
    int codeLength = 0;
    boolean doubleAllowed = false;
    int guessRounds = 0;
    String correctPostionSign = "";
    String correctCodeElementSign = "";

    // Eingabefeld und Ausgabefeld
    EditText inputEditText;
    ListView outputListView;

    // Eingaben als Strings
    List<String> itemList = new ArrayList<>();

    // Adapter
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputEditText = findViewById(R.id.inputEditText);

        // Initialvariablen aus Config File uebernehmen
        List<Row> configuratinEntries = getConfigEntries();

        for (Row entry : configuratinEntries) {
            switch (entry.getKey()) {
                case "alphabet":
                    alpahabet = entry.getValue().split(", ");
                    break;

                case "codeLength":
                    codeLength = Integer.valueOf(entry.getValue());
                    break;

                case "doubleAllowed":
                    doubleAllowed = Boolean.parseBoolean(entry.getValue());
                    break;

                case "guessRounds":
                    guessRounds = Integer.valueOf(entry.getValue());
                    break;

                case "correctPositionSign":
                    correctPostionSign = entry.getValue();
                    break;

                case "correctCodeElementSign": // Achtung! Leerzeichen
                    correctCodeElementSign = entry.getValue();
                    break;

                default:
                    System.err.println("Ungueltiger Paramter in Config File!");
            }
        }

        // ListView referenzieren zuweisen
        outputListView = findViewById(R.id.outputListView);

        // Adapter zu ListView zuweisen
        bindAdapterToListView(outputListView);
    }

    private String inputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        for (String line; (line = r.readLine()) != null; ) {
            total.append(line).append('\n');
        }
        return total.toString();
    }

    private List<Row> getConfigEntries() {
        // Config File als inputStream einlesen
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open("config.conf");
        }
        catch (IOException ex) {
            System.err.println("File konnte nicht gelesen werden!");
        }

        // inputStream in String umwandeln
        String configString = "";
        try {
            configString = inputStreamToString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // String in Zeilen umwandeln
        String[] keyValuePairs = configString.trim().split("\n");

        // String in Objekt mit key und value umwandeln
        return Arrays.stream(keyValuePairs).map(r -> {
            String[] temp = r.split("=");
            String key = temp[0];
            String values = temp[1];
            return new Row(key, values);
        }).collect(Collectors.toList());
    }

    @Override
    public void onClick(View v) {
        // Geratener Code des Users
        String userInput = inputEditText.getText().toString();

        // Leere Eingaben abfangen
        if (userInput.equals("")) {
            Toast.makeText(this, "Leere Eingabe", Toast.LENGTH_LONG).show();
            //userInput = findViewById(R.id.inputEditText).toString();
            return;
        }

        // Korrekte Codelaenge ueberpruefen
        if (userInput.length() != codeLength) {
            Toast.makeText(this, "Zu langer/kurzer Code!", Toast.LENGTH_LONG).show();
            //userInput = findViewById(R.id.inputEditText).toString();
            return;
        }

        // TODO Ungültige Zeichen abfangen

        addItemToList(userInput);
    }

    private void bindAdapterToListView(ListView lv) {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        outputListView.setAdapter(adapter);
    }

    private void addItemToList(String item) {
        // Zu interner Liste hinzufuegen
        itemList.add(item);

        // ListView aktualisieren
        adapter.notifyDataSetChanged();
    }

    private int generateRandomCode(int length, String[] alpahabet) {
        String randomCode = "";
        StringBuilder builder = new StringBuilder(randomCode);
        for (int i = 0; i < length; i++) {
            builder.append()
        }
    }
}
