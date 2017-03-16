package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Singleton class that stores an instance of {@link Context}.
 */
public class ContextHolder {

    private static ContextHolder instance = new ContextHolder();

    /**
     * Gets the singleton instance of this class.
     *
     * @return the singleton ContextHolder instance
     */
    public static ContextHolder getInstance() {
        return instance;
    }

    private Context context;

    private ContextHolder() {
    }

    /**
     * Sets the currently active context.
     *
     * @param context the context
     */
    void setContext(@Nullable Context context) {
        this.context = context;
    }

    /**
     * Determines whether a context currently exists.
     *
     * @return whether there is a context
     */
    public boolean hasContext() {
        return context != null;
    }

    /**
     * Gets the current context.
     *
     * @return the context
     * @throws IllegalStateException if there is no context
     */
    @Nonnull
    public Context getContext() {
        if (context == null) {
            throw new IllegalStateException();
        }
        return context;
    }

    public void ifPresent(@Nonnull Consumer<? super Context> then) {
        if (hasContext()) {
            then.accept(getContext());
        }
    }
}
