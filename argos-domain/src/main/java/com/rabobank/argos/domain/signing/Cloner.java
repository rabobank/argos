package com.rabobank.argos.domain.signing;

/*-
 * #%L
 * Argos Supply Chain Notary
 * %%
 * Copyright (C) 2019 Rabobank Nederland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.layout.rule.AllowRule;
import com.rabobank.argos.domain.layout.rule.CreateRule;
import com.rabobank.argos.domain.layout.rule.DeleteRule;
import com.rabobank.argos.domain.layout.rule.DisallowRule;
import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.domain.layout.rule.ModifyRule;
import com.rabobank.argos.domain.layout.rule.RequireRule;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
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
