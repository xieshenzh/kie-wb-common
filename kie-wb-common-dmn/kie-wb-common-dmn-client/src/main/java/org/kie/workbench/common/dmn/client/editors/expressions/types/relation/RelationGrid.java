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

package org.kie.workbench.common.dmn.client.editors.expressions.types.relation;

import java.util.ArrayList;
import java.util.Optional;

import javax.enterprise.event.Event;

import com.ait.lienzo.shared.core.types.EventPropagationMode;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.dmn.api.definition.HasExpression;
import org.kie.workbench.common.dmn.api.definition.HasName;
import org.kie.workbench.common.dmn.api.definition.v1_1.InformationItem;
import org.kie.workbench.common.dmn.api.definition.v1_1.List;
import org.kie.workbench.common.dmn.api.definition.v1_1.Relation;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.client.commands.expressions.types.relation.AddRelationColumnCommand;
import org.kie.workbench.common.dmn.client.commands.expressions.types.relation.AddRelationRowCommand;
import org.kie.workbench.common.dmn.client.commands.expressions.types.relation.DeleteRelationColumnCommand;
import org.kie.workbench.common.dmn.client.commands.expressions.types.relation.DeleteRelationRowCommand;
import org.kie.workbench.common.dmn.client.editors.expressions.util.SelectionUtils;
import org.kie.workbench.common.dmn.client.editors.types.NameAndDataTypePopoverView;
import org.kie.workbench.common.dmn.client.resources.i18n.DMNEditorConstants;
import org.kie.workbench.common.dmn.client.widgets.grid.BaseExpressionGrid;
import org.kie.workbench.common.dmn.client.widgets.grid.columns.factory.TextAreaSingletonDOMElementFactory;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.container.CellEditorControlsView;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.HasListSelectorControl;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.ListSelectorView;
import org.kie.workbench.common.dmn.client.widgets.grid.model.DMNGridRow;
import org.kie.workbench.common.dmn.client.widgets.grid.model.ExpressionEditorChanged;
import org.kie.workbench.common.dmn.client.widgets.grid.model.GridCellTuple;
import org.kie.workbench.common.dmn.client.widgets.layer.DMNGridLayer;
import org.kie.workbench.common.dmn.client.widgets.panel.DMNGridPanel;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.DomainObjectSelectionEvent;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandFactory;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.client.command.SessionCommandManager;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.command.util.CommandUtils;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;
import org.kie.workbench.common.stunner.forms.client.event.RefreshFormPropertiesEvent;
import org.uberfire.ext.wires.core.grids.client.model.GridColumn;
import org.uberfire.ext.wires.core.grids.client.widget.grid.columns.RowNumberColumn;

public class RelationGrid extends BaseExpressionGrid<Relation, RelationGridData, RelationUIModelMapper> implements HasListSelectorControl {

    private final TextAreaSingletonDOMElementFactory factory = getBodyTextAreaFactory();

    private final NameAndDataTypePopoverView.Presenter headerEditor;

    public RelationGrid(final GridCellTuple parent,
                        final Optional<String> nodeUUID,
                        final HasExpression hasExpression,
                        final Optional<Relation> expression,
                        final Optional<HasName> hasName,
                        final DMNGridPanel gridPanel,
                        final DMNGridLayer gridLayer,
                        final RelationGridData gridData,
                        final DefinitionUtils definitionUtils,
                        final SessionManager sessionManager,
                        final SessionCommandManager<AbstractCanvasHandler> sessionCommandManager,
                        final CanvasCommandFactory<AbstractCanvasHandler> canvasCommandFactory,
                        final Event<ExpressionEditorChanged> editorSelectedEvent,
                        final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent,
                        final Event<DomainObjectSelectionEvent> domainObjectSelectionEvent,
                        final CellEditorControlsView.Presenter cellEditorControls,
                        final ListSelectorView.Presenter listSelector,
                        final TranslationService translationService,
                        final int nesting,
                        final NameAndDataTypePopoverView.Presenter headerEditor) {
        super(parent,
              nodeUUID,
              hasExpression,
              expression,
              hasName,
              gridPanel,
              gridLayer,
              gridData,
              new RelationGridRenderer(),
              definitionUtils,
              sessionManager,
              sessionCommandManager,
              canvasCommandFactory,
              editorSelectedEvent,
              refreshFormPropertiesEvent,
              domainObjectSelectionEvent,
              cellEditorControls,
              listSelector,
              translationService,
              nesting);
        this.headerEditor = headerEditor;

        setEventPropagationMode(EventPropagationMode.NO_ANCESTORS);

        super.doInitialisation();
    }

    @Override
    protected void doInitialisation() {
        // Defer initialisation until after the constructor completes as
        // makeUiModelMapper needs expressionEditorDefinitionsSupplier to have been set
    }

    @Override
    public RelationUIModelMapper makeUiModelMapper() {
        return new RelationUIModelMapper(this::getModel,
                                         () -> expression,
                                         listSelector);
    }

    @Override
    public void initialiseUiColumns() {
        expression.ifPresent(e -> {
            model.appendColumn(new RowNumberColumn());
            e.getColumn().forEach(ii -> {
                final GridColumn relationColumn = makeRelationColumn(ii);
                model.appendColumn(relationColumn);
            });
        });

        getRenderer().setColumnRenderConstraint((isSelectionLayer, gridColumn) -> true);
    }

    private RelationColumn makeRelationColumn(final InformationItem informationItem) {
        final RelationColumn relationColumn = new RelationColumn(new RelationColumnHeaderMetaData(informationItem,
                                                                                                  clearDisplayNameConsumer(false),
                                                                                                  setDisplayNameConsumer(false),
                                                                                                  setTypeRefConsumer(),
                                                                                                  cellEditorControls,
                                                                                                  headerEditor,
                                                                                                  Optional.of(translationService.getTranslation(DMNEditorConstants.RelationEditor_EditRelation))),
                                                                 factory,
                                                                 this);
        return relationColumn;
    }

    @Override
    public void initialiseUiModel() {
        expression.ifPresent(e -> {
            e.getRow().forEach(r -> {
                int columnIndex = 0;
                model.appendRow(new DMNGridRow());
                uiModelMapper.fromDMNModel(model.getRowCount() - 1,
                                           columnIndex++);
                for (int ii = 0; ii < e.getColumn().size(); ii++) {
                    uiModelMapper.fromDMNModel(model.getRowCount() - 1,
                                               columnIndex++);
                }
            });
        });
    }

    @Override
    protected boolean isHeaderHidden() {
        return false;
    }

    @Override
    @SuppressWarnings("unused")
    public java.util.List<ListSelectorItem> getItems(final int uiRowIndex,
                                                     final int uiColumnIndex) {
        final java.util.List<ListSelectorItem> items = new ArrayList<>();
        final boolean isMultiRow = SelectionUtils.isMultiRow(model);
        final boolean isMultiColumn = SelectionUtils.isMultiColumn(model);

        items.add(ListSelectorTextItem.build(translationService.format(DMNEditorConstants.RelationEditor_InsertColumnLeft),
                                             !isMultiColumn && uiColumnIndex > 0,
                                             () -> {
                                                 cellEditorControls.hide();
                                                 expression.ifPresent(e -> addColumn(uiColumnIndex));
                                             }));
        items.add(ListSelectorTextItem.build(translationService.format(DMNEditorConstants.RelationEditor_InsertColumnRight),
                                             !isMultiColumn && uiColumnIndex > 0,
                                             () -> {
                                                 cellEditorControls.hide();
                                                 expression.ifPresent(e -> addColumn(uiColumnIndex + 1));
                                             }));
        items.add(ListSelectorTextItem.build(translationService.format(DMNEditorConstants.RelationEditor_DeleteColumn),
                                             !isMultiColumn && model.getColumnCount() - RelationUIModelMapperHelper.ROW_INDEX_COLUMN_COUNT > 1 && uiColumnIndex > 0,
                                             () -> {
                                                 cellEditorControls.hide();
                                                 expression.ifPresent(e -> deleteColumn(uiColumnIndex));
                                             }));
        items.add(new ListSelectorDividerItem());
        items.add(ListSelectorTextItem.build(translationService.format(DMNEditorConstants.RelationEditor_InsertRowAbove),
                                             !isMultiRow,
                                             () -> {
                                                 cellEditorControls.hide();
                                                 expression.ifPresent(e -> addRow(uiRowIndex));
                                             }));
        items.add(ListSelectorTextItem.build(translationService.format(DMNEditorConstants.RelationEditor_InsertRowBelow),
                                             !isMultiRow,
                                             () -> {
                                                 cellEditorControls.hide();
                                                 expression.ifPresent(e -> addRow(uiRowIndex + 1));
                                             }));
        items.add(ListSelectorTextItem.build(translationService.format(DMNEditorConstants.RelationEditor_DeleteRow),
                                             !isMultiRow && model.getRowCount() > 1,
                                             () -> {
                                                 cellEditorControls.hide();
                                                 expression.ifPresent(e -> deleteRow(uiRowIndex));
                                             }));
        return items;
    }

    @Override
    public void onItemSelected(final ListSelectorItem item) {
        final ListSelectorTextItem li = (ListSelectorTextItem) item;
        li.getCommand().execute();
    }

    void addColumn(final int index) {
        expression.ifPresent(relation -> {
            final InformationItem informationItem = new InformationItem();
            final RelationColumn relationColumn = makeRelationColumn(informationItem);
            informationItem.setName(new Name());

            final CommandResult<CanvasViolation> result = sessionCommandManager.execute((AbstractCanvasHandler) sessionManager.getCurrentSession().getCanvasHandler(),
                                                                                        new AddRelationColumnCommand(relation,
                                                                                                                     informationItem,
                                                                                                                     model,
                                                                                                                     relationColumn,
                                                                                                                     index,
                                                                                                                     uiModelMapper,
                                                                                                                     () -> resize(BaseExpressionGrid.RESIZE_EXISTING),
                                                                                                                     () -> resize(BaseExpressionGrid.RESIZE_EXISTING_MINIMUM)));

            if (!CommandUtils.isError(result)) {
                relationColumn.startEditingHeaderCell(0);
            }
        });
    }

    void deleteColumn(final int index) {
        expression.ifPresent(relation -> {
            sessionCommandManager.execute((AbstractCanvasHandler) sessionManager.getCurrentSession().getCanvasHandler(),
                                          new DeleteRelationColumnCommand(relation,
                                                                          model,
                                                                          index,
                                                                          uiModelMapper,
                                                                          () -> resize(BaseExpressionGrid.RESIZE_EXISTING_MINIMUM),
                                                                          () -> resize(BaseExpressionGrid.RESIZE_EXISTING)));
        });
    }

    void addRow(final int index) {
        expression.ifPresent(relation -> {
            sessionCommandManager.execute((AbstractCanvasHandler) sessionManager.getCurrentSession().getCanvasHandler(),
                                          new AddRelationRowCommand(relation,
                                                                    new List(),
                                                                    model,
                                                                    new DMNGridRow(),
                                                                    index,
                                                                    uiModelMapper,
                                                                    () -> resize(BaseExpressionGrid.RESIZE_EXISTING)));
        });
    }

    void deleteRow(final int index) {
        expression.ifPresent(relation -> {
            sessionCommandManager.execute((AbstractCanvasHandler) sessionManager.getCurrentSession().getCanvasHandler(),
                                          new DeleteRelationRowCommand(relation,
                                                                       model,
                                                                       index,
                                                                       () -> resize(BaseExpressionGrid.RESIZE_EXISTING)));
        });
    }
}
