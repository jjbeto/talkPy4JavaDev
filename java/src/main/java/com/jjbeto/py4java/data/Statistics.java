package com.jjbeto.py4java.data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Statistics {

    private String country;
    private Map<LocalDate, Row> values = new HashMap<>();

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Map<LocalDate, Row> getValues() {
        return values;
    }

    public void setValues(Map<LocalDate, Row> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Statistics that = (Statistics) o;
        return country.equals(that.country) &&
                values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, values);
    }

    @Override
    public String toString() {
        return "country:" + country + ",value=" + values;
    }

}
