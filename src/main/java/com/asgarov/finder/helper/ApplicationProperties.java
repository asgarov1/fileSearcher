package com.asgarov.finder.helper;

import static com.asgarov.finder.util.PropertiesReader.getProperties;

public class ApplicationProperties {

    public static final long DELAY_BETWEEN_NEW_REQUEST_CHECKS_IN_MS = Long.parseLong(getProperties().getProperty("DELAY_BETWEEN_NEW_REQUEST_CHECKS_IN_MS"));
    public static final String STANDARD_DIRECTORY = getProperties().getProperty("STANDARD_DIRECTORY");
    public static final String CHOOSE = getProperties().getProperty("CHOOSE");
    public static final String SEARCH = getProperties().getProperty("SEARCH");
    public static final String STOP = getProperties().getProperty("STOP");
    public static final String STOPPED_SEARCH_MESSAGE = getProperties().getProperty("STOPPED_SEARCH_MESSAGE");
    public static final String FILE_NAME = getProperties().getProperty("FILE_NAME");
    public static final String STARTING_DIRECTORY = getProperties().getProperty("STARTING_DIRECTORY");
    public static final String TITLE = getProperties().getProperty("TITLE");
    public static final String SEARCHING_MESSAGE = getProperties().getProperty("SEARCHING_MESSAGE");
    public static final String LINK_INSTRUCTIONS_MESSAGE = getProperties().getProperty("LINK_INSTRUCTIONS_MESSAGE");
    public static final String RESULTS_MAXED_OUT_MESSAGE = getProperties().getProperty("RESULTS_MAXED_OUT_MESSAGE");
    public static final int TEXT_FIELD_WIDTH = Integer.parseInt(getProperties().getProperty("TEXT_FIELD_WIDTH"));
    public static final int SPACING = Integer.parseInt(getProperties().getProperty("SPACING"));
    public static final int WINDOW_WIDTH = Integer.parseInt(getProperties().getProperty("WINDOW_WIDTH"));
    public static final int WINDOW_HEIGHT = Integer.parseInt(getProperties().getProperty("WINDOW_HEIGHT"));
    public static final int DEFAULT_DEPTH = Integer.parseInt(getProperties().getProperty("DEFAULT_DEPTH"));

}
