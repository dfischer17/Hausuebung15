package com.example.mastermind;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class Leaderboard {
    private static List<Score> scoreList = new ArrayList<>();

    public static void addScore(Score score) {
        scoreList.add(score);
    }

    public static void sortScores() {
        scoreList.sort((o1, o2) -> {
            if (o1.getRoundsPlayed() == o2.getRoundsPlayed()) {
                return o1.getTimeWon().compareTo(o2.getTimeWon());
            }
            return Integer.compare(o1.getRoundsPlayed(), o2.getRoundsPlayed());
        });
    }

    public static List<Score> getScoreList() {
        return scoreList;
    }

    public static void setScoreList(List<Score> scoreList) {
        Leaderboard.scoreList = scoreList;
    }
}
