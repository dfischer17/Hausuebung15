package com.example.mastermind;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // Konstanten
    final String saveFilename = "saveFile.txt";
    final String scoreFilename = "score.sc";

    // Initialvariablen
    List<String> alpahabet = new ArrayList();
    int codeLength = 0;
    boolean doubleAllowed = false;
    int guessRounds = 0;
    String correctPostionSign = "";
    String correctCodeElementSign = "";

    // Laufzeitvariablen
    Mode currentMode;
    int remainingGuesses = 0;
    LocalDateTime startTime;

    // Zu eratender Code
    String randomCode = "";

    // Eingabefeld und Ausgabefeld
    EditText inputEditText;
    ListView outputListView;

    // Eingaben als Strings
    List<String> itemList = new ArrayList<>();

    // Adapter
    ArrayAdapter<String> adapter;

    // DateTime formatter
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy(HH:mm)");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputEditText = findViewById(R.id.inputEditText);

        // Initialvariablen aus Config File uebernehmen
        List<Row> configuratinEntries = getConfigEntries();

        if (configuratinEntries.size() < 6)
            Toast.makeText(this, "Ungueltige config Datei!", Toast.LENGTH_LONG).show();

        for (Row entry : configuratinEntries) {
            switch (entry.getKey()) {
                case "alphabet":
                    alpahabet = Arrays.asList(entry.getValue().replaceAll(" ", "").split(","));
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

                case "correctCodeElementSign":
                    correctCodeElementSign = entry.getValue();
                    break;

                default:
                    System.err.println("Ungueltiger Paramter in Config File!");
            }
        }

        // Uebrigbleibende Runden
        remainingGuesses = guessRounds;

        // Zufallscode festlegen (aendert sich jede Runde!)
        setRandomCode();

        // ListView referenzieren zuweisen
        outputListView = findViewById(R.id.outputListView);

        // Adapter zu ListView zuweisen
        bindAdapterToListView(outputListView);

        // Eventhandler Klick auf Listeneintrag "START NEW GAME"
        outputListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                // Bei Settings und Score Mode ist letzter Eintrag Start New Game
                if ((currentMode.equals(Mode.SETTINGS) || currentMode.equals(Mode.SCORE)) && pos == itemList.size() - 1) {
                    startNewRound();
                }
                // Bei Game Mode muss dies nicht der Fall sein
                else if (currentMode.equals(Mode.ROUNDFINISHD)) {
                    String klickedText = itemList.get(pos);
                    if (klickedText.equals("NOT SOLVED") || klickedText.equals("SOLVED"))
                        startNewRound();
                }
            }
        });

        // Status Game
        currentMode = Mode.GAME;

        // Spielbeginn Zeit
        startTime = LocalDateTime.now();
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
        } catch (IOException ex) {
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
        // Tippen nur im Game Mode meoglich
        if (!currentMode.equals(Mode.GAME)) {
            Toast.makeText(this, "Submit nur im Spiel moeglich!", Toast.LENGTH_LONG).show();
            return;
        }

        // Geratener Code des Users
        String userInput = inputEditText.getText().toString().trim().replaceAll(",", "").replaceAll(" ", "");
        inputEditText.setText("");

        // Eingabe ueberpruefen
        if (!checkCode(userInput)) return;

        // Evaluierung des Versuchs
        StringBuilder outputBuilder = new StringBuilder(userInput + " | ");
        List<Integer> alreadyCorrectIndex = new ArrayList<>();
        char[] randomCodeCharacters = randomCode.toCharArray();
        char[] userInputCharacters = userInput.toCharArray();

        // Fuer jede Stelle an richtiger Postion "+" zu Ausgabe hinzufügen
        for (int i = 0; i < codeLength; i++) {
            if (userInputCharacters[i] == randomCodeCharacters[i]) {
                // Doppelbewertung vermeiden
                alreadyCorrectIndex.add(i);
                outputBuilder.append("+");
            }
        }

        // Fuer jede Stelle welche Teil des Codes ist "-" zu Ausgabe hinzufügen
        for (int i = 0; i < codeLength; i++) {
            if (!alreadyCorrectIndex.contains(i) && charArrayContains(randomCodeCharacters, userInputCharacters[i])) {
                outputBuilder.append("-");
            }
        }

        // Ausgabe
        String output = outputBuilder.toString();
        addItemToList(output);

        // Wenn Code richtig erraten
        if (userInput.equals(randomCode) && remainingGuesses > 0) {

            // Runde beendet
            currentMode = Mode.ROUNDFINISHD;

            // "solved" ausgeben
            addItemToList("SOLVED");

            // neuen Score in File speichern
            LocalDateTime timeWon = LocalDateTime.now();
            int roundsPlayed = guessRounds - remainingGuesses + 1;
            Duration roundTime = Duration.between(startTime, timeWon);
            Score score = new Score(timeWon, roundsPlayed, roundTime);
            saveScore(score);

            return;
        } else if (remainingGuesses <= 0) {
            // Runde beendet
            currentMode = Mode.ROUNDFINISHD;

            addItemToList("NOT SOLVED");
            addItemToList("Correct Code: " + randomCode);
            return;
        }

        // Versuch abziehen
        remainingGuesses--;
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

    private void addItemsToList(List<Score> object) {
        object.forEach(o -> itemList.add(o.toString()));

        // ListView aktualisieren
        adapter.notifyDataSetChanged();
    }

    private void clearList() {
        itemList.clear();
        adapter.notifyDataSetChanged();
    }

    private void setRandomCode() {
        // Zufallscode zuruecksetzen
        randomCode = "";

        // Neuen Zufallscode festlegen
        StringBuilder builder = new StringBuilder(randomCode);
        for (int i = 0; i < codeLength; i++) {
            int randomAlphabetCharacterIndex = (int) (Math.random() * codeLength);
            builder.append(alpahabet.get(randomAlphabetCharacterIndex));
        }

        randomCode = "1234";// TODO builder.toString();
    }

    public void showSettings(View v) {
        // Status Settings
        currentMode = Mode.SETTINGS;

        // Akutelles Spielfeld leeren
        itemList.clear();
        adapter.notifyDataSetChanged();

        // Row Objekte bestehen aus Key und Value
        List<Row> settings = getConfigEntries();

        // Jeweils Key und Value in itemList einfuegen
        for (Row setting : settings) {
            addItemToList(setting.getKey());
            addItemToList(setting.getValue());
        }

        // Eintrag zum neustarten
        addItemToList("START NEW GAME");
    }

    public void showScore(View v) {
        // Status Score
        currentMode = Mode.SCORE;

        // Spiel abbrechen -> alle Elmente aus der ListView löschen
        clearList();

        // Scores laden
        List<Score> loadedScores = loadScore();
        Leaderboard.setScoreList(loadedScores);

        // Scores sortieren
        Leaderboard.sortScores();

        // ListView aktualisieren
        List<Score> sortedScores = Leaderboard.getScoreList();
        addItemsToList(sortedScores);
        addItemToList("START NEW GAME");
    }

    public void save(View v) {
        // Speichern nur im Game Mode moeglich
        if (!currentMode.equals(Mode.GAME)) {
            Toast.makeText(this, "Speichern nur im Spiel moeglich!", Toast.LENGTH_LONG).show();
            return;
        }

        // Leeres speichern verhindern
        if (!(itemList.size() > 0)) {
            Toast.makeText(this, "Keine Tips zum Speichern!", Toast.LENGTH_LONG).show();
            return;
        }
        // String formatieren
        String output = generateSaveFileString();

        // In Datei speichern
        writeToSaveFile(output);

        // Neue Runde starten
        startNewRound();
    }

    public void load(View v) {
        // Nicht Laden wenn bereits geladen
        if (itemList.size() > 0 && currentMode.equals(Mode.GAME)) {
            Toast.makeText(this, "Spielstand bereits geladen!", Toast.LENGTH_LONG).show();
            return;
        }

        // Datei einlesen
        String[] rows = readFromSaveFile().split("\n");

        // Kein Spielstand vorhanden ueberpruefen
        if (!(rows.length > 1)) {
            Toast.makeText(this, "Kein Spielstand vorhanden!", Toast.LENGTH_LONG).show();
            return;
        }

        clearList();
        currentMode = Mode.GAME;

        // TODO Spielfeld laden
        randomCode = rows[1].replaceAll("<code>", "").replaceAll("</code>", "");

        // guesses einslesen
        for (int i = 2; i < rows.length - 1; i++) {
            // Für jeden guess
            String userInput = "";
            String result = "";

            if (rows[i].contains("<userInput>")) {
                userInput = rows[i].replaceAll("<userInput>", "").replaceAll("</userInput>", "");
                result = rows[i + 1].replaceAll("<result>", "").replaceAll("</result>", "");
                addItemToList(userInput + " | " + result);
            }
        }
    }

    private String generateSaveFileString() {
        // String formatieren
        String output = "";
        StringBuilder outputBuilder = new StringBuilder(output);

        // saveSate
        outputBuilder.append("<saveState>");
        outputBuilder.append("\n");

        // code
        outputBuilder.append("<code>" + randomCode + "</code>");
        outputBuilder.append("\n");

        // guesses
        for (int i = 1; i <= itemList.size(); i++) {
            // SOLVED Meldung ist kein guess
            if (itemList.get(i - 1).equals("SOLVED")) continue;

            // Umwandlung von for-each
            int item = i - 1; // guessNR vs itemNR
            int guessNumber = i;

            // guessXX
            outputBuilder.append("<guess" + guessNumber + ">");
            outputBuilder.append("\n");

            // userInput X,Y,Z
            // TODO Add comma between userinputs
            String userInput = itemList.get(item).replaceAll("\\+", "").replaceAll("-", "").replaceAll("\\|", "").trim();
            outputBuilder.append("<userInput>" + userInput + "</userInput>");
            outputBuilder.append("\n");

            // result
            // TODO Add comma between results
            String[] temp = itemList.get(item).split("\\|");
            String result = "";

            // TODO remove this if
            if (temp.length > 1) {
                result = temp[1].trim();
            }


            // TODO remove this if
            if (temp.length > 1) {
                outputBuilder.append("<result>" + result + "</result>");
                outputBuilder.append("\n");
            }

            // guessXX schliessen
            outputBuilder.append("</guess" + guessNumber + ">");
            outputBuilder.append("\n");
        }

        // saveState schliessen
        outputBuilder.append("</saveState>");

        return outputBuilder.toString();
    }

    private void writeToSaveFile(String output) {
        try {
            FileOutputStream fos = openFileOutput(saveFilename, MODE_PRIVATE);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
            out.println(output);
            out.flush();
            out.close();
        } catch (FileNotFoundException exp) {
            Log.d("mastermind", exp.getStackTrace().toString());
        }
    }

    private String readFromSaveFile() {
        String input = "";
        try {
            FileInputStream fis = openFileInput(saveFilename);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = in.readLine()) != null) {
                buffer.append(line + "\n");
            }
            input = buffer.toString();
            in.close();
        } catch (IOException exp) {
            Log.d("mastermind", exp.getStackTrace().toString());
        }
        return input;
    }

    private void saveScore(Score score) {
        writeToScoreFile(score.toCsvString());
    }

    private List<Score> loadScore() {
        List<Score> scores = new ArrayList<>();
        try {
            FileInputStream fis = openFileInput(scoreFilename);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = in.readLine()) != null) {
                String[] temp = line.split(";");
                int year = Integer.valueOf(temp[0]);
                int month = Integer.valueOf(temp[1]);
                int dayOfMonth = Integer.valueOf(temp[2]);
                int hour = Integer.valueOf(temp[3]);
                int minute = Integer.valueOf(temp[4]);

                LocalDateTime timeWon = LocalDateTime.of(year, month, dayOfMonth, hour, minute);
                int roundsWon = Integer.valueOf(temp[temp.length - 3]);

                long tempSeconds = Long.valueOf(temp[temp.length - 1]);
                Duration timePlayed = Duration.ofSeconds(tempSeconds);
                scores.add(new Score(timeWon, roundsWon, timePlayed));
            }
            in.close();
        } catch (IOException exp) {
            Log.d("mastermind", exp.getStackTrace().toString());
        }
        return scores;
    }

    private void writeToScoreFile(String output) {
        try {
            FileOutputStream fos = openFileOutput(scoreFilename, MODE_PRIVATE | MODE_APPEND);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
            out.println(output);
            out.flush();
            out.close();
        } catch (FileNotFoundException exp) {
            Log.d("mastermind", exp.getStackTrace().toString());
        }
    }

    private boolean checkCode(String userInput) {
        // Leere Eingaben abfangen
        if (userInput.equals("")) {
            Toast.makeText(this, "Leere Eingabe!", Toast.LENGTH_LONG).show();
            return false;
        }

        // Korrekte Codelaenge ueberpruefen
        if (userInput.length() < codeLength) {
            Toast.makeText(this, "Zu kurzer Code!", Toast.LENGTH_LONG).show();
            return false;
        } else if (userInput.length() > codeLength) {
            Toast.makeText(this, "Zu langer Code!", Toast.LENGTH_LONG).show();
            return false;
        }

        // Ungültige Zeichen abfangen
        List<String> inputCharacters = Arrays.asList(userInput.split(""));

        for (String curChar : inputCharacters) {
            if (!alpahabet.contains(curChar)) {
                Toast.makeText(this, "Ungueltige Eingabe!", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }

    private void startNewRound() {
        currentMode = Mode.GAME;
        setRandomCode();
        remainingGuesses = guessRounds;
        clearList();
        startTime = LocalDateTime.now();
    }

    private boolean charArrayContains(char[] charArray, char searchedChar) {
        boolean contains = false;
        for (char c : charArray) {
            if (c == searchedChar) {
                contains = true;
                break;
            }
        }
        return contains;
    }
}
