package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.service.domain.util.reflection.ParameterData;
import com.rabobank.argos.service.domain.util.reflection.ReflectionHelper;
import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultLocalPermissionCheckDataExtractorTest {

    private static final String LABEL_ID = "labelId";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    @Mock
    private ReflectionHelper reflectionHelper;
    @Mock
    private Method method;

    private Object[] simpleArgumentValues = {LABEL_ID, PARENT_LABEL_ID};

    private ObjectArgument objectArgumentValue = ObjectArgument
            .builder()
            .labelId(LABEL_ID)
            .parentLabelId(PARENT_LABEL_ID)
            .build();

    private Object[] objectArgumentValues = {objectArgumentValue};

    private DefaultLocalPermissionCheckDataExtractor defaultLocalPermissionCheckDataExtractor;

    @BeforeEach
    void setup() {
        defaultLocalPermissionCheckDataExtractor = new DefaultLocalPermissionCheckDataExtractor(reflectionHelper);

    }

    private void configureObjectArgumentValues() {
        ParameterData<LabelIdCheckParam, Object> labelIdCheckParameterData = new ParameterData<>(createLabelIdCheckParam("labelId"), objectArgumentValue);

        when(reflectionHelper
                .getParameterDataByAnnotation(method,
                        LabelIdCheckParam.class,
                        objectArgumentValues))
                .thenReturn(Optional.of(labelIdCheckParameterData));

        ParameterData<ParentLabelIdCheckParam, Object> parentLabelIdheckParameterData = new ParameterData<>(createParentLabelIdCheckParam("parentLabelId"),
                objectArgumentValue);

        when(reflectionHelper
                .getParameterDataByAnnotation(method,
                        ParentLabelIdCheckParam.class,
                        objectArgumentValues))
                .thenReturn(Optional.of(parentLabelIdheckParameterData));
    }

    private void configureSimpleArgumentValues() {
        ParameterData<LabelIdCheckParam, Object> labelIdCheckParameterData = new ParameterData<>(createLabelIdCheckParam(null), LABEL_ID);

        when(reflectionHelper
                .getParameterDataByAnnotation(eq(method),
                        eq(LabelIdCheckParam.class),
                        eq(simpleArgumentValues)))
                .thenReturn(Optional.of(labelIdCheckParameterData));

        ParameterData<ParentLabelIdCheckParam, Object> parentLabelIdCheckParameterData = new ParameterData<>(createParentLabelIdCheckParam(null), PARENT_LABEL_ID);

        when(reflectionHelper
                .getParameterDataByAnnotation(eq(method),
                        eq(ParentLabelIdCheckParam.class),
                        eq(simpleArgumentValues)))
                .thenReturn(Optional.of(parentLabelIdCheckParameterData));
    }

    @Test
    void extractLocalPermissionCheckDataWithSimpleArgumentShouldReturnValidLocalPermissionCheckData() {
        configureSimpleArgumentValues();
        LocalPermissionCheckData localPermissionCheckData = defaultLocalPermissionCheckDataExtractor.extractLocalPermissionCheckData(method, simpleArgumentValues);
        assertThat(localPermissionCheckData.getLabelId(), is(LABEL_ID));
        assertThat(localPermissionCheckData.getParentLabelId(), is(PARENT_LABEL_ID));
    }


    @Test
    void extractLocalPermissionCheckDataWithObjectArgumentShouldReturnValidLocalPermissionCheckData() {
        configureObjectArgumentValues();
        LocalPermissionCheckData localPermissionCheckData = defaultLocalPermissionCheckDataExtractor.extractLocalPermissionCheckData(method, objectArgumentValues);
        assertThat(localPermissionCheckData.getLabelId(), is(LABEL_ID));
        assertThat(localPermissionCheckData.getParentLabelId(), is(PARENT_LABEL_ID));
    }

    @Test
    void extractLocalPermissionCheckDataWithNoResultShouldReturnEmptyLocalPermissionCheckData() {
        configureOptionalEmpty();
        LocalPermissionCheckData localPermissionCheckData = defaultLocalPermissionCheckDataExtractor.extractLocalPermissionCheckData(method, objectArgumentValues);
        assertThat(localPermissionCheckData.getLabelId(), nullValue());
        assertThat(localPermissionCheckData.getParentLabelId(), nullValue());
    }

    private void configureOptionalEmpty() {
        when(reflectionHelper
                .getParameterDataByAnnotation(any(),
                        any(),
                        any()))
                .thenReturn(Optional.empty());

    }

    private LabelIdCheckParam createLabelIdCheckParam(final String path) {
        return new LabelIdCheckParam() {
            @Override
            public String propertyPath() {
                return path;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return LabelIdCheckParam.class;
            }
        };
    }

    private ParentLabelIdCheckParam createParentLabelIdCheckParam(final String path) {
        return new ParentLabelIdCheckParam() {
            @Override
            public String propertyPath() {
                return path;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ParentLabelIdCheckParam.class;
            }
        };
    }

    @Builder
    @Getter
    public static class ObjectArgument {
        private String labelId;
        private String parentLabelId;
    }
}