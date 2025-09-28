package com.example.valorquest.utils;

@FunctionalInterface
public interface RepositoryCallback<T> {
    void onComplete(T result);
}
