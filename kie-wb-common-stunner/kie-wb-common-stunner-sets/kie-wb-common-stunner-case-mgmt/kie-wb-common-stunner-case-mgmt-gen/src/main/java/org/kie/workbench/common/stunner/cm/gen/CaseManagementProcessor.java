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

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import org.kie.workbench.common.stunner.cm.function.CaseManagementPredicate;
import org.kie.workbench.common.stunner.cm.function.CaseManagementPredicateEvaluation;
import org.uberfire.annotations.processors.AbstractErrorAbsorbingProcessor;
import org.uberfire.annotations.processors.exceptions.GenerationException;

@SupportedAnnotationTypes({
        CaseManagementProcessor.ANNOTATION_CM_PREDICATE_EVALUATION,
        CaseManagementProcessor.ANNOTATION_CM_PREDICATE})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CaseManagementProcessor extends AbstractErrorAbsorbingProcessor {

    public final static String ANNOTATION_CM_PREDICATE_EVALUATION =
            "org.kie.workbench.common.stunner.cm.function.CaseManagementPredicateEvaluation";
    public final static String ANNOTATION_CM_PREDICATE =
            "org.kie.workbench.common.stunner.cm.function.CaseManagementPredicate";

    private CaseManagementPredicateEvaluatorGenerator predicateEvaluatorGenerator =
            new CaseManagementPredicateEvaluatorGenerator();

    @Override
    protected boolean processWithExceptions(Set<? extends TypeElement> set, RoundEnvironment roundEnv) throws Exception {
        //If prior processing threw an error exit
        if (roundEnv.errorRaised()) {
            return false;
        }

        if (!roundEnv.processingOver()) {
            final Elements elementUtils = processingEnv.getElementUtils();

            roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(ANNOTATION_CM_PREDICATE_EVALUATION)).forEach(this::processPredicateEvaluator);

            roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(ANNOTATION_CM_PREDICATE)).forEach(this::processPredicate);

            return true;
        } else {
            return processLastRound();
        }

    }

    private void processPredicateEvaluator(final Element e) {
        final boolean isClass = e.getKind() == ElementKind.INTERFACE;
        if (isClass) {
            final TypeElement classElement = (TypeElement) e;

            final String evaluatorClassName = getClassFullName(classElement);

            final CaseManagementPredicateEvaluation evaluatorAnnotation = classElement.getAnnotation(CaseManagementPredicateEvaluation.class);

            note("Discovered @CaseManagementPredicateEvaluator for type [" + classElement.getSimpleName().toString() + "]");

            predicateEvaluatorGenerator.addEvaluator(evaluatorAnnotation.evaluator(),
                                                     evaluatorAnnotation.packageName(),
                                                     evaluatorAnnotation.className(),
                                                     evaluatorClassName,
                                                     evaluatorAnnotation.predicateKeyType(),
                                                     evaluatorAnnotation.predicateArgumentType(),
                                                     evaluatorAnnotation.additionalImports());

        }
    }

    private void processPredicate(final Element e) {
        final boolean isClass = e.getKind() == ElementKind.CLASS;
        if (isClass) {
            final TypeElement classElement = (TypeElement) e;

            final String predicateClassName = getClassFullName(classElement);

            final CaseManagementPredicate predicateAnnotation = classElement.getAnnotation(CaseManagementPredicate.class);

            note("Discovered @CaseManagementPredicate for type [" + classElement.getSimpleName().toString() + "]");

            predicateEvaluatorGenerator.addEvalutorPredicate(predicateAnnotation.evaluator(),
                                                     predicateAnnotation.predicateKey(),
                                                     predicateClassName,
                                                     predicateAnnotation.additionalImports());

        }
    }



    private boolean processLastRound() throws Exception {
        return predicateEvaluatorGenerator.generateCode(p -> {
            note("Starting generation for CaseManagementPredicateEvaluator named [" + p.getClassName() + "]");
            try {
                final StringBuffer result = predicateEvaluatorGenerator.generate(p);

                writeCode(p.getPackageName(),
                          p.getClassName(),
                          result);

                return true;
            } catch (IOException | GenerationException e) {
                error(e.getMessage());
                return false;
            }
        });
    }

    private void note(String message) {
        log(Diagnostic.Kind.NOTE,
            message);
    }

    private void error(String message) {
        log(Diagnostic.Kind.ERROR,
            message);
    }

    private void log(Diagnostic.Kind kind, String message) {
        final Messager messager = processingEnv.getMessager();
        messager.printMessage(kind, message);
    }

    private String getClassFullName(TypeElement classElement) {
        final PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
        final String name = classElement.getSimpleName().toString();
        final String packageName = packageElement.getQualifiedName().toString();

        return packageName + "." + name;
    }
}
