package com.rabobank.argos.domain.signing;

import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * By defining all methods, we force MapStruct to generate new objects for each mapper in stead of
 * taking shortcuts by mapping an object directly.
 */
@Mapper
public interface Cloner {

    Link clone(Link link);

    List<Artifact> cloneArtifacts(List<Artifact> artifacts);

    Artifact clone(Artifact artifact);

    Layout clone(Layout layout);

    List<Step> cloneSteps(List<Step> steps);

    Step clone(Step step);

    List<Rule> clone(List<Rule> rules);

    default Rule clone(Rule rule) {
        if (rule instanceof MatchRule) {
            return clone((MatchRule) rule);
        } else {
            return new Rule(rule.getRuleType(), rule.getPattern());
        }

    }

    MatchRule clone(MatchRule matchRule);

}
