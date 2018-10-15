/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.dmn.client.editors.expressions.types.function;

import java.util.Optional;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.dmn.api.definition.HasExpression;
import org.kie.workbench.common.dmn.api.definition.HasName;
import org.kie.workbench.common.dmn.api.definition.v1_1.FunctionDefinition;
import org.kie.workbench.common.dmn.api.definition.v1_1.LiteralExpression;
import org.kie.workbench.common.dmn.api.qualifiers.DMNEditor;
import org.kie.workbench.common.dmn.client.editors.expressions.types.BaseEditorDefinition;
import org.kie.workbench.common.dmn.client.editors.expressions.types.ExpressionEditorDefinitions;
import org.kie.workbench.common.dmn.client.editors.expressions.types.ExpressionType;
import org.kie.workbench.common.dmn.client.editors.expressions.types.function.parameters.ParametersPopoverView;
import org.kie.workbench.common.dmn.client.editors.types.NameAndDataTypePopoverView;
import org.kie.workbench.common.dmn.client.resources.i18n.DMNEditorConstants;
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
import org.kie.workbench.common.stunner.core.client.session.Session;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;
import org.kie.workbench.common.stunner.forms.client.event.RefreshFormPropertiesEvent;

@ApplicationScoped
public class FunctionEditorDefinition extends BaseEditorDefinition<FunctionDefinition, DMNGridData> {

    private Supplier<ExpressionEditorDefinitions> expressionEditorDefinitionsSupplier;
    private Supplier<ExpressionEditorDefinitions> supplementaryEditorDefinitionsSupplier;

    private NameAndDataTypePopoverView.Presenter headerEditor;
    private ParametersPopoverView.Presenter parametersEditor;

    public FunctionEditorDefinition() {
        //CDI proxy
    }

    @Inject
    public FunctionEditorDefinition(final DefinitionUtils definitionUtils,
                                    final SessionManager sessionManager,
                                    final @Session SessionCommandManager<AbstractCanvasHandler> sessionCommandManager,
                                    final CanvasCommandFactory<AbstractCanvasHandler> canvasCommandFactory,
                                    final Event<ExpressionEditorChanged> editorSelectedEvent,
                                    final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent,
                                    final Event<DomainObjectSelectionEvent> domainObjectSelectionEvent,
                                    final ListSelectorView.Presenter listSelector,
                                    final TranslationService translationService,
                                    final @DMNEditor Supplier<ExpressionEditorDefinitions> expressionEditorDefinitionsSupplier,
                                    final @FunctionGridSupplementaryEditor Supplier<ExpressionEditorDefinitions> supplementaryEditorDefinitionsSupplier,
                                    final NameAndDataTypePopoverView.Presenter headerEditor,
                                    final ParametersPopoverView.Presenter parametersEditor) {
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
        this.supplementaryEditorDefinitionsSupplier = supplementaryEditorDefinitionsSupplier;
        this.headerEditor = headerEditor;
        this.parametersEditor = parametersEditor;
    }

    @Override
    public ExpressionType getType() {
        return ExpressionType.FUNCTION;
    }

    @Override
    public String getName() {
        return translationService.format(DMNEditorConstants.ExpressionEditor_FunctionType);
    }

    @Override
    public Optional<FunctionDefinition> getModelClass() {
        return Optional.of(new FunctionDefinition());
    }

    @Override
    public void enrich(final Optional<String> nodeUUID,
                       final Optional<FunctionDefinition> expression) {
        expression.ifPresent(function -> {
            KindUtilities.setKind(function,
                                  FunctionDefinition.Kind.FEEL);
            final LiteralExpression le = new LiteralExpression();
            function.setExpression(le);
            le.setParent(function);
        });
    }

    @Override
    public Optional<BaseExpressionGrid> getEditor(final GridCellTuple parent,
                                                  final Optional<String> nodeUUID,
                                                  final HasExpression hasExpression,
                                                  final Optional<FunctionDefinition> expression,
                                                  final Optional<HasName> hasName,
                                                  final int nesting) {
        return Optional.of(new FunctionGrid(parent,
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
                                            expressionEditorDefinitionsSupplier,
                                            supplementaryEditorDefinitionsSupplier,
                                            headerEditor,
                                            parametersEditor));
    }

    @Override
    protected DMNGridData makeGridData(final Optional<FunctionDefinition> expression) {
        return new DMNGridData();
    }
}
