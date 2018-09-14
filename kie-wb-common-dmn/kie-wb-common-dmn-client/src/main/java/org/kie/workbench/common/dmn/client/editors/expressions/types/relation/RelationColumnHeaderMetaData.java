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

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.kie.workbench.common.dmn.api.definition.HasName;
import org.kie.workbench.common.dmn.api.definition.HasTypeRef;
import org.kie.workbench.common.dmn.api.definition.v1_1.InformationItem;
import org.kie.workbench.common.dmn.api.definition.v1_1.Relation;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.api.property.dmn.QName;
import org.kie.workbench.common.dmn.client.editors.types.NameAndDataTypeEditorView;
import org.kie.workbench.common.dmn.client.widgets.grid.columns.NameAndDataTypeHeaderMetaData;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.container.CellEditorControlsView;

public class RelationColumnHeaderMetaData extends NameAndDataTypeHeaderMetaData<Relation> {

    private static final String NAME_DATA_TYPE_COLUMN_GROUP = "RelationColumnHeaderMetaData$NameAndDataTypeColumn";

    public RelationColumnHeaderMetaData(final InformationItem variable,
                                        final Consumer<HasName> clearDisplayNameConsumer,
                                        final BiConsumer<HasName, Name> setDisplayNameConsumer,
                                        final BiConsumer<HasTypeRef, QName> setTypeRefConsumer,
                                        final CellEditorControlsView.Presenter cellEditorControls,
                                        final NameAndDataTypeEditorView.Presenter headerEditor) {
        super(Optional.ofNullable(variable),
              variable,
              clearDisplayNameConsumer,
              setDisplayNameConsumer,
              setTypeRefConsumer,
              cellEditorControls,
              headerEditor);
    }

    @Override
    public String getColumnGroup() {
        return NAME_DATA_TYPE_COLUMN_GROUP;
    }
}
