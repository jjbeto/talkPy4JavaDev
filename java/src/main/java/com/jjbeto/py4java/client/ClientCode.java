package com.jjbeto.py4java.client;

public class ClientCode {

    private final String targetServer = "http://localhost:8080/api/";
    private final String sourceUrl = "https://raw.githubusercontent.com/jjbeto/talkPy4JavaDevResources/master/johns_hopkins/";
    private final String confirmedPath = "time_series_covid19_confirmed_global.csv";
    private final String deathsPath = "time_series_covid19_deaths_global.csv";
    private final String recoveredPath = "time_series_covid19_confirmed_global.csv";

    // step 1: load the csv from the url
    // step 2: read csv header and and load lines
    // step 3: for each line, convert it to a data object
    // step 4: add extra data (sick and percentage change)

}
