package com.youzan.mobile.enjoydependence.auto

import com.youzan.mobile.enjoydependence.auto.all.AutoPublishAllExt
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 更新.lif文件中信息
 */
class UpdateLifTask extends DefaultTask {

    private String glc
    private String lbv
    AutoPublishAllExt autoPublishAllExt

    @TaskAction
    void update() {
        if (project.hasProperty("glc") && project.glc != "unspecified") {
            glc = String.valueOf(project.glc)
        }
        if (project.hasProperty("lbv") && project.lbv != "unspecified") {
            lbv = String.valueOf(project.lbv)
        }

        autoPublishAllExt = getProject().getExtensions().findByType(AutoPublishAllExt.class)
        if (autoPublishAllExt != null && autoPublishAllExt.getGlcParentPath() != null) {
            File glcFile = new File(autoPublishAllExt.getGlcParentPath() + "/" + ".lif")
            LIFManager lifManager = LIFManager.getInstance(glcFile.getAbsolutePath())
            lifManager.setGLCId(glc)
            lifManager.setLBV(lbv)
            project.logger.error(lifManager.writeIntoLIF())
        }
    }

    @Override
    public String getGroup() {
        return "enjoyDependence"
    }

    @Override
    public String getDescription() {
        return "update last info file"
    }
}