package com.youzan.mobile.enjoydependence.auto;

import com.youzan.mobile.enjoydependence.auto.all.AutoPublishAllExt;

import org.apache.tools.ant.Task;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class LoadLifTask extends DefaultTask {

    AutoPublishAllExt autoPublishAllExt;

    @TaskAction
    void autoPublish() {
        autoPublishAllExt = getProject().getExtensions().findByType(AutoPublishAllExt.class);
        if (autoPublishAllExt != null && autoPublishAllExt.getGlcParentPath() != null) {
            File glcFile = new File(autoPublishAllExt.getGlcParentPath() + "/" + ".lif");
            LIFManager lifManager = LIFManager.getInstance(glcFile.getAbsolutePath());
            getProject().getLogger().error(lifManager.loadGLCId());
            getProject().getLogger().error(lifManager.loadBLV());
        }

    }

    @Override
    public String getGroup() {
        return "enjoyDependence";
    }

    @Override
    public String getDescription() {
        return "load last info file";
    }
}
