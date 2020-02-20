package com.rabobank.argos.service.domain.util.reflection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Parameter;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ParameterData<T, S> {
    private T annotation;

    private Parameter parameter;

    private S value;
}
