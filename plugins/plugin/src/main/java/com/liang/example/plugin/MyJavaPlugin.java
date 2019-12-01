package com.liang.example.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MyJavaPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().create("hello", Greeting.class, (task) -> {
            task.setMessage("Hello");
            task.setRecipient("World");
        });
    }
}
