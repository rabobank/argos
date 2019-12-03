package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestRule;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;



@Mapper(componentModel = "spring")
public abstract class RuleMapper {

    @Autowired
    private MatchRuleMapper matchRuleMapper;


    @ObjectFactory
    public Rule createRule(RestRule restRule) {
        switch (restRule.getRuleType()) {
            case MATCH:
                return matchRuleMapper.mapFromRestRule(restRule);
            default:
                return new Rule(RuleType.valueOf(restRule.getRuleType().name()), restRule.getPattern());
        }
    }

    @ObjectFactory
    public RestRule createRule(Rule rule) {
        if (rule instanceof MatchRule) {
            return matchRuleMapper.mapToRestRule((MatchRule) rule);
        }
        return new RestRule();
    }

    public abstract Rule mapFromRestRule(RestRule restRule);

    public abstract RestRule mapFromRestRule(Rule rule);


}

