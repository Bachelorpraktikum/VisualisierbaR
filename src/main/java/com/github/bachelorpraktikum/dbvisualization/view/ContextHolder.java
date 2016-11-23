package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.model.Context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContextHolder {
    private static ContextHolder instance = new ContextHolder();

    public static ContextHolder getInstance() {
        return instance;
    }

    private Context context;

    private ContextHolder() {
    }

    void setContext(@Nullable Context context) {
        this.context = context;
    }

    @Nonnull
    Context getContext() {
        if (context == null) {
            throw new IllegalStateException();
        }
        return context;
    }
}
