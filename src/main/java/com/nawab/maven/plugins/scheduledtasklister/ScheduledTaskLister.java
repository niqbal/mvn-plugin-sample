package com.nawab.maven.plugins.scheduledtasklister;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

@Component
/**
 * This is not being used.
 */
public class ScheduledTaskLister {

    private static class ScheduledTaskInfo {
        String className;
        String methodName;
        long fixedRate;
        String description;

        ScheduledTaskInfo(String className, String methodName, long fixedRate, String description) {
            this.className = className;
            this.methodName = methodName;
            this.fixedRate = fixedRate;
            this.description = description;
        }

        @Override
        public String toString() {
            return className + "," + methodName + "," + fixedRate + "," + description;
        }
    }

    @Autowired
    private ApplicationContext context;

    private ClassPathScanningCandidateComponentProvider createScanner() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        TypeFilter filter = new AnnotationTypeFilter(Scheduled.class);
        scanner.addIncludeFilter(filter);
        return scanner;
    }

    public void listScheduledTasks() {
        System.out.println("inside listScheduledTasks");
        Map<String, ScheduledTaskInfo> existingTasks = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("scheduledTasks.csv"))) {
            String line;
            reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                existingTasks.put(parts[0] + "." + parts[1], new ScheduledTaskInfo(parts[0], parts[1], Long.parseLong(parts[2]), parts[3]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<ScheduledTaskInfo> taskInfoList = new ArrayList<>();
        ClassPathScanningCandidateComponentProvider scanner = createScanner();

//        context.getBea
        for (String basePackage : context.getBeanDefinitionNames()) {
            scanner.findCandidateComponents(basePackage).forEach(beanDefinition -> {
                try {
                    System.out.println("beanDefinition.getBeanClassName() = " + beanDefinition.getBeanClassName());
                    Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Scheduled.class)) {
                            // ... process the scheduled method ...
                            String description = existingTasks.containsKey(clazz.getName() + "." + method.getName()) ? existingTasks.get(clazz.getName() + "." + method.getName()).description : "DESC NEEDED";
                            taskInfoList.add(new ScheduledTaskInfo(clazz.getName(),
                                    method.getName(),
                                    method.getAnnotation(Scheduled.class).fixedRate(),
                                    description));
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }

        // Sort taskInfoList
        taskInfoList.sort(Comparator.comparing(o -> o.className));

        try (FileWriter writer = new FileWriter("scheduledTasks.csv")) {
            writer.write("Class Name,Method Name,Scheduled Fixed Rate,Description\n");
            for (ScheduledTaskInfo info : taskInfoList) {
                writer.write(info.toString() + "\n");
                if ("DESC NEEDED".equals(info.description)) {
                    System.out.println("Error: Description needed for " + info.className + "." + info.methodName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
