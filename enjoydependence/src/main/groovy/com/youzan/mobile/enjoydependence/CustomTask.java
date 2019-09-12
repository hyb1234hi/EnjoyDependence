package com.youzan.mobile.enjoydependence;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class CustomTask extends DefaultTask {

    @TaskAction
    public void say(){
        System.out.println("我执行了，我是个task");
    }
}
