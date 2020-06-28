package com.jjbeto.py4java.client;

import com.jjbeto.py4java.data.Row;
import com.jjbeto.py4java.data.Statistics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;

public class ClientCode {

    private final String targetServer = "http://localhost:8080/api/";
    private final String sourceUrl = "https://raw.githubusercontent.com/jjbeto/talkPy4JavaDevResources/master/johns_hopkins/";
    private final String confirmedPath = "time_series_covid19_confirmed_global.csv";
    private final String deathsPath = "time_series_covid19_deaths_global.csv";
    private final String recoveredPath = "time_series_covid19_recovered_global.csv";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) throws Exception {
        ClientCode clientCode = new ClientCode();

        List<Statistics> confirmed = clientCode.loadStatistics(clientCode.confirmedPath);
        List<Statistics> deaths = clientCode.loadStatistics(clientCode.deathsPath);
        List<Statistics> recovered = clientCode.loadStatistics(clientCode.recoveredPath);
        List<Statistics> sick = clientCode.loadSickStatistics(confirmed, deaths, recovered);

        clientCode.sendStatistics("confirmed", confirmed);
        clientCode.sendStatistics("deaths", deaths);
        clientCode.sendStatistics("recovered", recovered);
        clientCode.sendStatistics("sick", sick);
    }

    public void sendStatistics(String name, List<Statistics> statistics) throws URISyntaxException {
        URI uri = new URI(targetServer + name + "/java");
        statistics.forEach(data -> {
            ResponseEntity<String> result = restTemplate.postForEntity(uri, data, String.class);
            System.out.println(result.getStatusCode());
        });
    }

    public List<Statistics> loadStatistics(String path) throws IOException {
        String content = readStringFromURL(sourceUrl + path);

        List<Statistics> statistics = loadData(content);
        addPctChange(statistics);
        return statistics;
    }

    // step 1: load the csv from the url
    private String readStringFromURL(String target) throws IOException {
        try (InputStream in = new URL(target).openStream()) {
            return new String(in.readAllBytes(), UTF_8);
        }
    }

    // step 2: read csv header and and load lines
    private List<Statistics> loadData(String content) {
        // read complete header of dates
        String rawHeader = content.lines().findFirst().get();
        List<LocalDate> header = new ArrayList<>();
        stream(rawHeader.split(","))
                .skip(1) // ignore "country" cell
                .forEach(value -> header.add(LocalDate.parse(value, formatter)));

        List<Statistics> listOfData = new ArrayList<>();
        content.lines()
                .skip(1) // ignore header
                .forEach(line -> listOfData.add(convert(header, line)));

        return listOfData;
    }

    // step 3: for each line, convert it to a data object
    private Statistics convert(List<LocalDate> headers, String line) {
        String[] split = line.split(",");
        if (split.length != headers.size() + 1) {
            // lets avoid too much complexity and not deal with csv related scenarios
            return null;
        }

        Statistics data = new Statistics();
        data.setCountry(split[0]);
        for (int i = 1; i < headers.size(); i++) {
            data.getValues().put(headers.get(i - 1), new Row(Integer.parseInt(split[i])));
        }
        return data;
    }

    // step 4: add extra data (percentage change)
    private void addPctChange(List<Statistics> statistics) {
        statistics.forEach(stat -> {
            for (Map.Entry<LocalDate, Row> row : stat.getValues().entrySet()) {
                Row first = row.getValue();
                Row second = stat.getValues().get(row.getKey().plusDays(1));
                if (second == null || (first.getValue() == 0 && second.getValue() == 0)) {
                    continue;
                }

                if (first.getValue() == 0 && second.getValue() != 0) {
                    second.setPctChange(1.0);
                } else {
                    second.setPctChange((second.getValue() - first.getValue()) / first.getValue().doubleValue());
                }
            }
        });
    }

    // step 4: add extra data (sick)
    public List<Statistics> loadSickStatistics(List<Statistics> confirmed, List<Statistics> deaths, List<Statistics> recovered) {
        List<Statistics> sickList = new ArrayList<>();
        for (Statistics stat : confirmed) {
            Statistics sick = new Statistics();
            sick.setCountry(stat.getCountry());

            Statistics deathStats = deaths.stream().filter(stats -> stats.getCountry().equals(stat.getCountry())).findFirst().orElse(null);
            Statistics recoveredStats = recovered.stream().filter(stats -> stats.getCountry().equals(stat.getCountry())).findFirst().orElse(null);
            for (Map.Entry<LocalDate, Row> row : stat.getValues().entrySet()) {
                int deathNum = getValue(deathStats, row);
                int recoveredNum = getValue(recoveredStats, row);
                int sickNum = row.getValue().getValue() - deathNum - recoveredNum;
                sick.getValues().put(row.getKey(), new Row(sickNum));
            }
            sickList.add(sick);
        }

        addPctChange(sickList);
        return sickList;
    }

    private int getValue(Statistics statistics, Map.Entry<LocalDate, Row> row) {
        Row deathRow = statistics != null ? statistics.getValues().get(row.getKey()) : null;
        if (deathRow != null) {
            return deathRow.getValue();
        } else {
            return 0;
        }
    }

}
