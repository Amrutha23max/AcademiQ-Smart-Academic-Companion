package com.academiq.common;

import java.util.List;

/** Generic repository contract implemented by the DAOs. */
public interface AcademicRepository<T> {
    int save(T item) throws DatabaseException;       // returns generated id
    void update(T item) throws DatabaseException;
    void delete(int id) throws DatabaseException;
    T findById(int id) throws DatabaseException;
    List<T> findAll() throws DatabaseException;      // for the logged-in user
}
