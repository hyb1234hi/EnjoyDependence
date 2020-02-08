package com.youzan.mobile.enjoydependence.annotations

class AnnotationClassInfo {

    public String pluginName
    public String className
    public String hasRegister

    AnnotationClassInfo(String pluginName, String className, boolean hasRegister) {
        this.pluginName = pluginName
        this.className = className
        this.hasRegister = hasRegister
    }
}