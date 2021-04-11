package com.asgarov.finder.helper;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;

import java.nio.file.Paths;

import static com.asgarov.finder.service.EventHandler.handleLinkClickedEvent;

public class UIHelper {
    /**
     * Helper method to create a TextField
     *
     * @param maxWidth
     * @return
     */
    public static TextField createTextField(int maxWidth) {
        TextField textField = new TextField();
        textField.setMaxWidth(maxWidth);
        return textField;
    }

    public static TextField createTextField(int maxWidth, String text) {
        TextField textField = new TextField();
        textField.setMaxWidth(maxWidth);
        textField.setText(text);
        return textField;
    }

    public static VBox createCenteredVBox() {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        return box;
    }

    /**
     * Helper method to create a Label
     *
     * @param labelText
     * @param color
     * @param font
     * @return
     */
    public static Label createLabel(String labelText, Color color, Font font) {
        Label label = new Label(labelText);
        label.setTextFill(color);
        label.setFont(font);
        return label;
    }

    /**
     * Helper method to create a button
     *
     * @param text
     * @return
     */
    public static Button createButton(String text) {
        Button button = new Button();
        button.setText(text);
        return button;
    }

    public static Hyperlink mapToHyperlink(String result) {
        Hyperlink link = new Hyperlink(result);
        link.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> handleLinkClickedEvent(e, result));
        return link;
    }

    public static Label createBlackLabel(String text) {
        return createLabel(text, Color.BLACK, Font.font("Arial", 15));
    }

    public static Label createGrayLabel(String text) {
        return createLabel(text, Color.GRAY, Font.font("Arial", 15));
    }

    public static DirectoryChooser createDirectoryChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(Paths.get(System.getProperty("user.dir")).toFile());
        return directoryChooser;
    }
}
