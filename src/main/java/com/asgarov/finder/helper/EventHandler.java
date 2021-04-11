package com.asgarov.finder.helper;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.nio.file.Paths;

import static java.awt.Desktop.getDesktop;

public class EventHandler {
    public static void handleLinkClickedEvent(MouseEvent e, String result) {
        switch (e.getButton()) {
            case PRIMARY: openFile(result); break;
            case SECONDARY: copyToClipboard(result); break;
        }
    }

    private static void openFile(String result) {
        try {
            getDesktop().open(Paths.get(result).getParent().toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyToClipboard(String result) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(result);
        clipboard.setContent(content);
    }
}
