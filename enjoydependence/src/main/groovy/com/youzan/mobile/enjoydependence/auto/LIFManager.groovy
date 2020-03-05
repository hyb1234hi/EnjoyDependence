package com.youzan.mobile.enjoydependence.auto

import groovy.json.JsonSlurper

public class LIFManager {

    private volatile static LIFManager instance;
    private String lifPath;
    private LastInfoFile lastInfoFile;

    private LIFManager(String lifPath) {
        this.lifPath = lifPath;
        File lifFile = new File(lifPath);
        if (lifFile.exists()) {
            lastInfoFile = (LastInfoFile) new JsonSlurper().parse(lifFile);
        }

    }

    public static LIFManager getInstance(String lifPath) {
        Objects.requireNonNull(lifPath);
        if (instance == null) {
            synchronized (LIFManager.class) {
                if (instance == null) {
                    instance = new LIFManager(lifPath);
                }
            }
        }

        return instance;
    }

    public String loadGLCId() {
        if (lastInfoFile != null) {
            return lastInfoFile.glc;
        }
        return "";
    }

    public String loadBLV() {
        if (lastInfoFile != null) {
            return lastInfoFile.lbv;
        }
        return "";
    }
}