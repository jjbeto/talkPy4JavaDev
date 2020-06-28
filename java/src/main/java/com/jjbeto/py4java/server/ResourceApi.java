package com.jjbeto.py4java.server;

import com.jjbeto.py4java.data.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ResourceApi {

    private static final Logger logger = LoggerFactory.getLogger(ResourceApi.class.getName());

    private final Map<String, Set<Statistics>> confirmed = new HashMap<>();
    private final Map<String, Set<Statistics>> deaths = new HashMap<>();
    private final Map<String, Set<Statistics>> recovered = new HashMap<>();
    private final Map<String, Set<Statistics>> sick = new HashMap<>();

    @DeleteMapping("/{type}/{src}")
    public void deleteStatistics(@PathVariable("type") String type, @PathVariable("src") String src) {
        getData(type, src).clear();
        logger.info("[" + type + "] data was deleted");
    }

    @GetMapping("/{type}/{src}")
    public Set<Statistics> getStatistics(@PathVariable("type") String type, @PathVariable("src") String src) {
        Set<Statistics> data = getData(type, src);
        logger.info("Returning [" + type + "] information");
        return data;
    }

    @GetMapping("/diff/{type}")
    public Set<Statistics> getStatistics(@PathVariable("type") String type) {
        Set<Statistics> java = getData(type, "java");
        Set<Statistics> python = getData(type, "python");
        Set<Statistics> result = new HashSet<>();
        for (Statistics jStat : java) {
            Statistics pyStat = python.stream().filter(p -> p.getCountry().equals(jStat.getCountry())).findFirst().get();
            boolean allMatch = jStat.getValues().entrySet().stream().allMatch(e -> e.getValue().equals(pyStat.getValues().get(e.getKey())));
            if (allMatch) {
                continue;
            }
            result.add(jStat);
        }
        return result;
    }

    @PostMapping("/{type}/{src}")
    public void postStatistics(@PathVariable("type") String type, @PathVariable("src") String src, @RequestBody Statistics statistics) {
        getData(type, src).add(statistics);
        logger.info("New [" + type + "]: " + statistics);
    }

    private Set<Statistics> getData(String type, String src) {
        switch (type) {
            case "confirmed":
                return confirmed.computeIfAbsent(src, data -> new HashSet<>());
            case "deaths":
                return deaths.computeIfAbsent(src, data -> new HashSet<>());
            case "recovered":
                return recovered.computeIfAbsent(src, data -> new HashSet<>());
            case "sick":
                return sick.computeIfAbsent(src, data -> new HashSet<>());
            default:
                throw new IllegalArgumentException(type + " is invalid");
        }
    }

}
