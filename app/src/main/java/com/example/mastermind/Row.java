package com.example.mastermind;

public class Row {
    private String key;
    private String value;

    public Row(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key.trim();
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value.trim();
    }

    public void setValue(String value) {
        this.value = value;
    }
}
