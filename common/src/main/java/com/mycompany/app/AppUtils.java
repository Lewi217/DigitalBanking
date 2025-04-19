package com.mycompany.app;

import java.util.function.Consumer;

public class AppUtils {

    public static <T>  void updateField(Consumer<T> setter, T newValue, T currentValue) {
        if (newValue != null) {
            setter.accept(newValue);
        }
    }
}
