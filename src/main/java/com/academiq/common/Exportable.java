package com.academiq.common;

/** A record that can be written as one CSV row. */
public interface Exportable {
    String[] csvHeader();
    String[] csvRow();
}
