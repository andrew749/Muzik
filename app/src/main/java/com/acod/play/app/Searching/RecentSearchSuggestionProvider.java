package com.acod.play.app.Searching;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by Andrew on 6/25/2014.
 */
public class RecentSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.acod.RecentSearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public RecentSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}

