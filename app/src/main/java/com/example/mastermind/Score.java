package com.example.mastermind;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Score {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy(HH:mm)");
    private LocalDateTime timeWon;
    private int roundsPlayed;
    private Duration timePlayed;

    public Score(LocalDateTime timeWon, int roundsPlayed, Duration timePlayed) {
        this.timeWon = timeWon;
        this.roundsPlayed = roundsPlayed;
        this.timePlayed = timePlayed;
    }

    @Override
    public String toString() {
        return getTimeWon().format(dateFormatter) + " | " + getRoundsPlayed() + " Round(s)" + " | " + secondsFormatter(timePlayed.getSeconds());
    }

    public String toCsvString() {
        int dayOfMonth = getTimeWon().getDayOfMonth();
        int month = getTimeWon().getMonth().getValue();
        int year = getTimeWon().getYear();
        int hour = getTimeWon().getHour();
        int minute = getTimeWon().getMinute();
        int roundsPlayed = getRoundsPlayed();

        return year + ";" + month + ";" + dayOfMonth + ";" + hour + ";" + minute + ";" + roundsPlayed + ";" + timePlayed.toMinutes() + ";" + timePlayed.getSeconds();
    }

    public LocalDateTime getTimeWon() {
        return timeWon;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    private String secondsFormatter(long input) {
        long minutes = (input % 3600) / 60;
        long seconds = input % 60;

        return minutes + " min " + seconds + " sec";
    }
}
