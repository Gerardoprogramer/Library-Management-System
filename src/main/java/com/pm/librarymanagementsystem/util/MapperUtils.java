package com.pm.librarymanagementsystem.util;

import java.util.function.Consumer;

public final class MapperUtils {

    private MapperUtils() {}

    public static <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
