package com.jjbeto.py4java.data;

import java.util.Objects;

public class Row {

    private Integer value;

    private Double pctChange;

    public Row() {
    }

    public Row(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Double getPctChange() {
        return pctChange;
    }

    public void setPctChange(Double pctChange) {
        this.pctChange = pctChange;
    }

    @Override
    public String toString() {
        return "value=" + value + ",pctChange=" + pctChange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Row row = (Row) o;
        return value.equals(row.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

}
