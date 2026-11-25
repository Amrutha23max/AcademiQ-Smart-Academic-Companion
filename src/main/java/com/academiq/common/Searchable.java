package com.academiq.common;

import java.util.List;

/** Common search behaviour. */
public interface Searchable<T> {
    List<T> search(String keyword) throws DatabaseException;
}
