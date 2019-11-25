package com.rabobank.argos.domain.signing;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.Layout;
import com.rabobank.argos.domain.model.Link;
import com.rabobank.argos.domain.model.Step;
import com.rabobank.argos.domain.model.rule.AllowRule;
import com.rabobank.argos.domain.model.rule.CreateRule;
import com.rabobank.argos.domain.model.rule.DeleteRule;
import com.rabobank.argos.domain.model.rule.DisallowRule;
import com.rabobank.argos.domain.model.rule.MatchRule;
import com.rabobank.argos.domain.model.rule.ModifyRule;
import com.rabobank.argos.domain.model.rule.RequireRule;
import com.rabobank.argos.domain.model.rule.Rule;
import org.mapstruct.Mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        try {
            Method clone = this.getClass().getMethod("clone", rule.getClass());
            return (Rule) clone.invoke(this, rule);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

    AllowRule clone(AllowRule allowRule);

    CreateRule clone(CreateRule createRule);

    DeleteRule clone(DeleteRule deleteRule);

    DisallowRule clone(DisallowRule disAllowRule);

    MatchRule clone(MatchRule matchRule);

    ModifyRule clone(ModifyRule modifyRule);

    RequireRule clone(RequireRule requireRule);

}
