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

package org.kie.workbench.common.stunner.cm.gen;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.uberfire.annotations.processors.exceptions.GenerationException;

public class CaseManagementPredicateEvaluatorGenerator {

    private static final String TEMPLATE_NAME = "CaseManagementPredicateEvaluator.ftl";

    protected static ExceptionInInitializerError INITIALIZER_EXCEPTION = null;

    private final Configuration config;

    private final ConcurrentMap<String, EvaluatorDetails> evaluatorsMap;

    public CaseManagementPredicateEvaluatorGenerator() {
        try {
            this.config = new Configuration();
            this.config.setClassForTemplateLoading(this.getClass(), "templates");
            this.config.setObjectWrapper(new DefaultObjectWrapper());
        } catch (NoClassDefFoundError var2) {
            if (var2.getCause() == null) {
                var2.initCause(INITIALIZER_EXCEPTION);
            }
            throw var2;
        } catch (ExceptionInInitializerError var3) {
            INITIALIZER_EXCEPTION = var3;
            throw var3;
        }

        evaluatorsMap = new ConcurrentHashMap<>();
    }

    private EvaluatorDetails getEvaluator(String evaluator) {
        EvaluatorDetails evaluatorDetails = evaluatorsMap.get(evaluator);
        if (evaluatorDetails == null) {
            EvaluatorDetails newEvaluatorDetails = new EvaluatorDetails();
            evaluatorDetails = evaluatorsMap.putIfAbsent(evaluator, newEvaluatorDetails);
            if (evaluatorDetails == null) {
                evaluatorDetails = newEvaluatorDetails;
            }
        }
        return evaluatorDetails;
    }

    public void addEvaluator(final String evaluator,
                             final String packageName,
                             final String className,
                             final String interfaceName,
                             final String predicateKeyType,
                             final String predicateArgumentType,
                             final String[] imports) {
        EvaluatorDetails evaluatorDetails = getEvaluator(evaluator);
        evaluatorDetails.setPackageName(packageName);
        evaluatorDetails.setClassName(className);
        evaluatorDetails.setInterfaceName(interfaceName);
        evaluatorDetails.setPredicateKeyType(predicateKeyType);
        evaluatorDetails.setPredicateArgumentType(predicateArgumentType);
        evaluatorDetails.addAdditinalImports(imports);
    }

    public void addEvalutorPredicate(final String evaluator,
                                     final String predicateKey,
                                     final String predicateClassName,
                                     final String[] imports) {
        EvaluatorDetails evaluatorDetails = getEvaluator(evaluator);
        evaluatorDetails.addPredicate(predicateKey, predicateClassName);
        evaluatorDetails.addAdditinalImports(imports);

    }

    public boolean generateCode(Predicate<EvaluatorDetails> codeGenerator) {
        return this.evaluatorsMap.entrySet().stream().allMatch(entry -> codeGenerator.test(entry.getValue()));
    }

    public StringBuffer generate(EvaluatorDetails evaluator) throws GenerationException {

        Map<String, Object> context = new HashMap<>();

        context.put("package", evaluator.getPackageName());
        context.put("additionalImports", evaluator.getAdditinalImports());
        context.put("genClassName", this.getClass().getName());
        context.put("className", evaluator.getClassName());
        context.put("interfaceName", evaluator.getInterfaceName());
        context.put("keyType", evaluator.getPredicateKeyType());
        context.put("argumentType", evaluator.getPredicateArgumentType());
        context.put("predicatesNum", evaluator.getPredicateDetails().size());
        context.put("predicateDetails", evaluator.getPredicateDetails());

        //Generate code
        try (final StringWriter sw = new StringWriter();
             final BufferedWriter bw = new BufferedWriter(sw)) {

            final Template template = config.getTemplate(TEMPLATE_NAME);
            template.process(context, bw);

            return sw.getBuffer();
        } catch (IOException | TemplateException ex) {
            throw new GenerationException(ex);
        }
    }

    public static class PredicateDetails {
        final private String key;
        final private String predicateClass;

        PredicateDetails(final String key, final String predicateClass) {
            this.key = key;
            this.predicateClass = predicateClass;
        }

        public String getKey() {
            return key;
        }

        public String getPredicateClass() {
            return predicateClass;
        }
    }

    static class EvaluatorDetails {
        private String packageName;
        private String className;
        private String interfaceName;
        private String predicateKeyType;
        private String predicateArgumentType;

        final private List<String> additinalImports;
        final private List<PredicateDetails> predicateDetails;

        EvaluatorDetails() {
            this.additinalImports = new LinkedList<>();
            this.predicateDetails = new LinkedList<>();
        }

        void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        void setClassName(String className) {
            this.className = className;
        }

        void setInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
        }

        void setPredicateKeyType(String predicateKeyType) {
            this.predicateKeyType = predicateKeyType;
        }

        void setPredicateArgumentType(String predicateArgumentType) {
            this.predicateArgumentType = predicateArgumentType;
        }

        void addAdditinalImports(String[] imports) {
            if (imports != null && imports.length > 0) {
                Arrays.stream(imports).forEach(additinalImports::add);
            }
        }

        void addPredicate(final String predicateKey, final String predicateClassName) {
            this.predicateDetails.add(new PredicateDetails(predicateKey, predicateClassName));
        }

        String getPackageName() {
            return packageName;
        }

        String getClassName() {
            return className;
        }

        String getInterfaceName() {
            return interfaceName;
        }

        String getPredicateKeyType() {
            return predicateKeyType;
        }

        String getPredicateArgumentType() {
            return predicateArgumentType;
        }

        List<String> getAdditinalImports() {
            return additinalImports;
        }

        List<PredicateDetails> getPredicateDetails() {
            return predicateDetails;
        }
    }
}
