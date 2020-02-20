package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.service.domain.util.reflection.ParameterData;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.rabobank.argos.service.domain.util.reflection.ReflectionHelper.getParameterDataByAnnotation;

@Component
public class DefaultLabelCheckDataExtractor implements LabelCheckDataExtractor {
    final BeanUtilsBean beanutils = new BeanUtilsBean();

    @Override
    public Optional<LabelCheckData> extractLabelCheckData(Method method, Object[] argumentValues) {

        List<ParameterData> labelIdCheckParameterData = getParameterDataByAnnotation(method,
                LabelIdCheckParam.class,
                argumentValues);

        LabelCheckData.LabelCheckDataBuilder builder = LabelCheckData.builder();
        if (!labelIdCheckParameterData.isEmpty()) {

            ParameterData<LabelIdCheckParam, String> parameter = labelIdCheckParameterData.iterator().next();

            if (parameter.getAnnotation().propertyPath() == null) {
                builder.labelId(parameter.getValue());
            } else {
                try {
                    String value = beanutils.
                            getProperty(parameter.getValue(),
                                    parameter.getAnnotation().propertyPath());
                    builder.labelId(value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            return Optional.of(builder.build());
        } else {
            return Optional.empty();
        }


    }

}
