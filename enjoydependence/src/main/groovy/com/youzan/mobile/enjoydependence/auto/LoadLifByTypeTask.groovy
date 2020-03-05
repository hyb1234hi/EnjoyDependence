package com.youzan.mobile.enjoydependence.auto

import com.youzan.mobile.enjoydependence.auto.all.AutoPublishAllExt
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 获取打包信息by type
 */
class LoadLifByTypeTask extends DefaultTask {

    private String lifType
    AutoPublishAllExt autoPublishAllExt;

    @TaskAction
    loadLifByTypeTask() {
        if (project.hasProperty("lifType") && project.lifType != "unspecified") {
            lifType = String.valueOf(project.lifType)
        }

        if (lifType != null) {
            autoPublishAllExt = project.extensions.findByType(AutoPublishAllExt.class)
            if (autoPublishAllExt != null && autoPublishAllExt.getGlcParentPath() != null) {
                File glcFile = new File(autoPublishAllExt.getGlcParentPath() + "/" + ".lif")
                LIFManager lifManager = LIFManager.getInstance(glcFile.getAbsolutePath())
                def msg = ""
                if (lifType == "glc") {
                    msg = lifManager.loadGLCId()
                } else if (lifType == "lbv") {
                    msg = lifManager.loadLBV()
                }

                File lifTemp = new File(autoPublishAllExt.getGlcParentPath() + "/" + ".lifTemp")
                if (!lifTemp.exists()) {
                    lifTemp.createNewFile()
                }

                lifTemp.text = msg
                project.logger.error(msg)
            }
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