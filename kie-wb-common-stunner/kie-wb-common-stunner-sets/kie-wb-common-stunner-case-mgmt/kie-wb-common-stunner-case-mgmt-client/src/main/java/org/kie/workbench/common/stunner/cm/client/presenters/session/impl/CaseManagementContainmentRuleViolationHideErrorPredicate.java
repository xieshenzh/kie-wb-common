/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.cm.client.presenters.session.impl;

import java.util.function.Predicate;

import org.kie.workbench.common.stunner.cm.function.CaseManagementPredicate;
import org.kie.workbench.common.stunner.core.rule.RuleViolation;
import org.kie.workbench.common.stunner.core.rule.violations.ContainmentRuleViolation;

@CaseManagementPredicate(
        evaluator = "RuleViolationHideError",
        predicateKey = "ContainmentRuleViolation.class",
        additionalImports = {"org.kie.workbench.common.stunner.core.rule.violations.ContainmentRuleViolation"})
public class CaseManagementContainmentRuleViolationHideErrorPredicate
        implements Predicate<RuleViolation> {

    @Override
    public boolean test(RuleViolation ruleViolation) {
        return ruleViolation instanceof ContainmentRuleViolation
                && ((ContainmentRuleViolation) ruleViolation).isParentMatch("org.kie.workbench.common.stunner.cm.CaseManagementDefinitionSet");
    }
}
