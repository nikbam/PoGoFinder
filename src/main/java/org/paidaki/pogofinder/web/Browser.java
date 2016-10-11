package org.paidaki.pogofinder.web;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class Browser extends Region {

    private static final String HOMEPAGE = Browser.class.getResource("/html/index.html").toExternalForm();

    private WebView webView = new WebView();
    private Bridge bridge = new Bridge(this);

    public JSObject jsWindow;
    public WebEngine webEngine = webView.getEngine();

    public Browser() {
        webEngine.load(HOMEPAGE);
        getChildren().add(webView);

        jsWindow = (JSObject) webEngine.executeScript("window");
        jsWindow.setMember(Bridge.JS_BRIDGE_NAME, bridge);
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(webView, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return 1200;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 800;
    }
}
