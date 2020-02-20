package com.rabobank.argos.service.domain.util.reflection;

import com.codepoetics.protonpack.StreamUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class ReflectionHelper {
    private ReflectionHelper() {

    }

    public static List<ParameterData> getParameterDataByType(Method method, Class type, Object[] argumentValues) {
        return StreamUtils
                .zipWithIndex(asList(method.getParameters()).stream())
                .filter(p -> p.getValue().getType().equals(type))
                .map(p -> ParameterData
                        .builder()
                        .parameter(p.getValue())
                        .value(getArgumentValueByIndex(argumentValues, (int) p.getIndex()))
                        .build()
                )
                .collect(Collectors.toList());
    }

    public static List<ParameterData> getParameterDataByAnnotation(Method method, Class<? extends Annotation> annotation, Object[] argumentValues) {
        return StreamUtils
                .zipWithIndex(Arrays.stream(method.getParameters()))
                .filter(p -> p.getValue().getAnnotation(annotation) != null)
                .map(p -> ParameterData
                        .builder()
                        .parameter(p.getValue())
                        .value(getArgumentValueByIndex(argumentValues, (int) p.getIndex()))
                        .annotation(p.getValue().getAnnotation(annotation))
                        .build()
                )
                .collect(Collectors.toList());
    }

    private static Object getArgumentValueByIndex(Object[] argumentValues, Integer index) {
        try {
            return argumentValues[index];
        } catch (Exception e) {
            throw new IllegalArgumentException("argument value at index: " + index + " does not exist", e);
        }
    }

}
