package com.youzan.mobile.enjoydependence.auto

import groovy.json.JsonOutput
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

    String loadLBV() {
        if (lastInfoFile != null) {
            return lastInfoFile.lbv
        }
        return ""
    }

    void setGLCId(String glc) {
        if (glc != null && glc != "") {
            lastInfoFile.glc = glc
        }
    }

    void setLBV(String lbv) {
        if (lbv != null && lbv != null) {
            lastInfoFile.lbv = lbv
        }
    }

    String writeIntoLIF() {
        def json = JsonOutput.toJson(lastInfoFile)
        File file = new File(lifPath)
        if (!file.exists()) {
            file.createNewFile()
        }

        file.text = json
        return json
    }
}