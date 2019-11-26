package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestStep;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RuleMapper.class})
public interface StepMapper {

    @Mapping(source = "name", target = "stepName")
    Step mapFromRestStep(RestStep restStep);

    @InheritInverseConfiguration
    RestStep mapToRestStep(Step step);
}
