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

package org.kie.workbench.common.dmn.client.editors.expressions.types.function.supplementary;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.enterprise.event.Event;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.dmn.api.definition.HasExpression;
import org.kie.workbench.common.dmn.api.definition.HasName;
import org.kie.workbench.common.dmn.api.definition.v1_1.Context;
import org.kie.workbench.common.dmn.api.definition.v1_1.ContextEntry;
import org.kie.workbench.common.dmn.api.definition.v1_1.InformationItem;
import org.kie.workbench.common.dmn.api.definition.v1_1.LiteralExpression;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.client.editors.expressions.types.BaseEditorDefinition;
import org.kie.workbench.common.dmn.client.editors.expressions.types.ExpressionEditorDefinitions;
import org.kie.workbench.common.dmn.client.widgets.grid.BaseExpressionGrid;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.ListSelectorView;
import org.kie.workbench.common.dmn.client.widgets.grid.model.DMNGridData;
import org.kie.workbench.common.dmn.client.widgets.grid.model.ExpressionEditorChanged;
import org.kie.workbench.common.dmn.client.widgets.grid.model.GridCellTuple;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.DomainObjectSelectionEvent;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandFactory;
import org.kie.workbench.common.stunner.core.client.command.SessionCommandManager;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;
import org.kie.workbench.common.stunner.forms.client.event.RefreshFormPropertiesEvent;

public abstract class BaseSupplementaryFunctionEditorDefinition extends BaseEditorDefinition<Context, FunctionSupplementaryGridData> {

    private Supplier<ExpressionEditorDefinitions> expressionEditorDefinitionsSupplier;

    protected BaseSupplementaryFunctionEditorDefinition() {
        //CDI proxy
    }

    public BaseSupplementaryFunctionEditorDefinition(final DefinitionUtils definitionUtils,
                                                     final SessionManager sessionManager,
                                                     final SessionCommandManager<AbstractCanvasHandler> sessionCommandManager,
                                                     final CanvasCommandFactory<AbstractCanvasHandler> canvasCommandFactory,
                                                     final Event<ExpressionEditorChanged> editorSelectedEvent,
                                                     final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent,
                                                     final Event<DomainObjectSelectionEvent> domainObjectSelectionEvent,
                                                     final ListSelectorView.Presenter listSelector,
                                                     final TranslationService translationService,
                                                     final Supplier<ExpressionEditorDefinitions> expressionEditorDefinitionsSupplier) {
        super(definitionUtils,
              sessionManager,
              sessionCommandManager,
              canvasCommandFactory,
              editorSelectedEvent,
              refreshFormPropertiesEvent,
              domainObjectSelectionEvent,
              listSelector,
              translationService);
        this.expressionEditorDefinitionsSupplier = expressionEditorDefinitionsSupplier;
    }

    @Override
    public Optional<Context> getModelClass() {
        return Optional.of(new Context());
    }

    @Override
    public void enrich(final Optional<String> nodeUUID,
                       final Optional<Context> expression) {
        expression.ifPresent(context -> {
            getVariableNames().forEach(name -> {
                final ContextEntry contextEntry = new ContextEntry();
                final InformationItem variable = new InformationItem();
                variable.setName(new Name(name));
                contextEntry.setVariable(variable);
                contextEntry.setExpression(new LiteralExpression());
                context.getContextEntry().add(contextEntry);
                contextEntry.setParent(context);
            });
        });
    }

    @Override
    public Optional<BaseExpressionGrid> getEditor(final GridCellTuple parent,
                                                  final Optional<String> nodeUUID,
                                                  final HasExpression hasExpression,
                                                  final Optional<Context> expression,
                                                  final Optional<HasName> hasName,
                                                  final int nesting) {
        return Optional.of(new FunctionSupplementaryGrid(parent,
                                                         nodeUUID,
                                                         hasExpression,
                                                         expression,
                                                         hasName,
                                                         getGridPanel(),
                                                         getGridLayer(),
                                                         makeGridData(expression),
                                                         definitionUtils,
                                                         sessionManager,
                                                         sessionCommandManager,
                                                         canvasCommandFactory,
                                                         editorSelectedEvent,
                                                         refreshFormPropertiesEvent,
                                                         domainObjectSelectionEvent,
                                                         getCellEditorControls(),
                                                         listSelector,
                                                         translationService,
                                                         nesting,
                                                         expressionEditorDefinitionsSupplier));
    }

    @Override
    protected FunctionSupplementaryGridData makeGridData(final Optional<Context> expression) {
        return new FunctionSupplementaryGridData(new DMNGridData(),
                                                 sessionManager,
                                                 sessionCommandManager,
                                                 expression,
                                                 getGridLayer()::batch);
    }

    protected abstract List<String> getVariableNames();
}
