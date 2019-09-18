package com.youzan.mobile.enjoydependence.build

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

class BuildTimeCostPlugin implements Plugin<Project> {

    //用来记录 task 的执行时长等信息
    Map<String, TaskExecTimeInfo> timeCostMap = new HashMap<>()
    //用来按顺序记录执行的 task 名称
    List<String> taskPathList = new ArrayList<>()

    @Override
    void apply(Project project) {
        BuildTimeCostExt timeCostExt = project.getExtensions().create("taskExecTime", BuildTimeCostExt)
        project.getGradle().addListener(new TaskExecutionListener() {
            @Override
            void beforeExecute(Task task) {
                TaskExecTimeInfo timeInfo = new TaskExecTimeInfo()
                timeInfo.path = task.path
                timeInfo.start = System.currentTimeMillis()
                timeCostMap.put(task.path, timeInfo)
                taskPathList.add(task.path)
            }

            @Override
            void afterExecute(Task task, TaskState taskState) {
                TaskExecTimeInfo timeInfo = timeCostMap.get(task.path)
                if (timeInfo != null) {
                    timeInfo.end = System.currentTimeMillis()
                    timeInfo.total = timeInfo.end - timeInfo.start
                }
            }
        })

        project.getGradle().addBuildListener(new BuildListener() {
            @Override
            void buildStarted(Gradle gradle) {
            }

            @Override
            void settingsEvaluated(Settings settings) {

            }

            @Override
            void projectsLoaded(Gradle gradle) {

            }

            @Override
            void projectsEvaluated(Gradle gradle) {

            }

            @Override
            void buildFinished(BuildResult buildResult) {
                if (!timeCostExt.enable) {
                    return
                }
                println "---------------------------------------"
                println "---------------------------------------"
                println "${buildResult.action} finished, now println all task execution time:"

                if (timeCostExt.sorted) {
                    //进行排序
                    List<TaskExecTimeInfo> list = new ArrayList<>()
                    for (Map.Entry<String, TaskExecTimeInfo> entry : timeCostMap) {
                        list.add(entry.value)
                    }
                    Collections.sort(list, new Comparator<TaskExecTimeInfo>() {
                        @Override
                        int compare(TaskExecTimeInfo t1, TaskExecTimeInfo t2) {
                            return t2.total - t1.total
                        }
                    })
                    for (TaskExecTimeInfo timeInfo : list) {
                        long t = timeInfo.total
                        if (t >= timeCostExt.threshold) {
                            println("${timeInfo.path}  [${t}ms]")
                        }
                    }
                } else {
                    //按 task 执行顺序打印出执行时长信息
                    for (String path : taskPathList) {
                        long t = timeCostMap.get(path).total
                        if (t >= timeCostExt.threshold) {
                            println("${path}  [${t}ms]")
                        }
                    }
                }
                println "---------------------------------------"
                println "---------------------------------------"
            }
        })
    }
}