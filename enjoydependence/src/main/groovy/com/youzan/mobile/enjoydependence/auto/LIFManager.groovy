package com.youzan.mobile.enjoydependence.auto

import groovy.json.JsonSlurper

class LIFManager {

    private volatile static LIFManager instance
    private String lifPath
    private def lastInfoFile

    private LIFManager(String lifPath) {
        this.lifPath = lifPath
        File lifFile = new File(lifPath)
        if (lifFile.exists()) {
            lastInfoFile = new JsonSlurper().parse(lifFile)
        }

    }

    static LIFManager getInstance(String lifPath) {
        Objects.requireNonNull(lifPath)
        if (instance == null) {
            synchronized (LIFManager.class) {
                if (instance == null) {
                    instance = new LIFManager(lifPath)
                }
            }
        }

        return instance
    }

    static void clearInstance() {
        instance = null
    }

    String loadGLCId() {
        if (lastInfoFile != null) {
            return lastInfoFile.glc
        }
        return ""
    }

    String loadBLV() {
        if (lastInfoFile != null) {
            return lastInfoFile.lbv
        }
        return ""
    }
}