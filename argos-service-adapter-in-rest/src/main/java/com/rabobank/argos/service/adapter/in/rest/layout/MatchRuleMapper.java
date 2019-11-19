package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.model.rule.MatchRule;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestRule;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MatchRuleMapper {

    @Mappings({
            @Mapping(source = "destinationPathPrefix", target = "destinationPathPrefix")
    })
    MatchRule mapFromRestRule(RestRule restRule);

    @InheritInverseConfiguration
    RestRule mapToRestRule(MatchRule rule);
}
