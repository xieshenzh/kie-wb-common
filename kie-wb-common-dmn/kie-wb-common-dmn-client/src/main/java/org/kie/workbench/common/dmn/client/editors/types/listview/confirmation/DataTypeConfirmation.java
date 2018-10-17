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

package org.kie.workbench.common.dmn.client.editors.types.listview.confirmation;

import java.util.Objects;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.kie.workbench.common.dmn.api.definition.v1_1.ItemDefinition;
import org.kie.workbench.common.dmn.client.editors.types.common.DataType;
import org.kie.workbench.common.dmn.client.editors.types.common.DataTypeManager;
import org.kie.workbench.common.dmn.client.editors.types.messages.DataTypeFlashMessage;
import org.kie.workbench.common.dmn.client.editors.types.persistence.DataTypeStore;
import org.kie.workbench.common.dmn.client.editors.types.persistence.ItemDefinitionStore;
import org.uberfire.mvp.Command;

/**
 * Fires warning messages to get confirmation in potentially destructive Data Type operations.
 */
@Dependent
public class DataTypeConfirmation {

    private final DataTypeManager dataTypeManager;

    private final DataTypeStore dataTypeStore;

    private final ItemDefinitionStore itemDefinitionStore;

    private final Event<DataTypeFlashMessage> flashMessageEvent;

    private final DataTypeHasFieldsWarningMessage dataTypeHasFieldsWarningMessage;

    private final ReferencedDataTypeWarningMessage referencedDataTypeWarningMessage;

    @Inject
    public DataTypeConfirmation(final DataTypeManager dataTypeManager,
                                final DataTypeStore dataTypeStore,
                                final ItemDefinitionStore itemDefinitionStore,
                                final Event<DataTypeFlashMessage> flashMessageEvent,
                                final DataTypeHasFieldsWarningMessage dataTypeHasFieldsWarningMessage,
                                final ReferencedDataTypeWarningMessage referencedDataTypeWarningMessage) {

        this.dataTypeManager = dataTypeManager;
        this.dataTypeStore = dataTypeStore;
        this.itemDefinitionStore = itemDefinitionStore;
        this.flashMessageEvent = flashMessageEvent;
        this.dataTypeHasFieldsWarningMessage = dataTypeHasFieldsWarningMessage;
        this.referencedDataTypeWarningMessage = referencedDataTypeWarningMessage;
    }

    public void ifDataTypeDoesNotHaveLostSubDataTypes(final DataType dataType,
                                                      final Command onSuccess,
                                                      final Command onError) {

        if (hasLostSubDataTypes(dataType)) {
            flashMessageEvent.fire(dataTypeHasFieldsWarningMessage.getFlashMessage(dataType, onSuccess, onError));
        } else {
            onSuccess.execute();
        }
    }

    public void ifIsNotReferencedDataType(final DataType dataType,
                                          final Command onSuccess) {

        if (isReferencedByAnotherDataType(dataType)) {
            flashMessageEvent.fire(referencedDataTypeWarningMessage.getFlashMessage(dataType, onSuccess, () -> { /* Nothing. */ }));
        } else {
            onSuccess.execute();
        }
    }

    private boolean isReferencedByAnotherDataType(final DataType dataType) {
        return dataTypeStore.all().stream().anyMatch(dt -> Objects.equals(dt.getType(), dataType.getName()));
    }

    private boolean hasLostSubDataTypes(final DataType dataType) {

        final ItemDefinition itemDefinition = itemDefinitionStore.get(dataType.getUUID());
        final boolean isDataTypeNotStructure = !isStructure(dataType);
        final boolean hasItemDefinitionSubItemDefinitions = !itemDefinition.getItemComponent().isEmpty();

        return isDataTypeNotStructure && hasItemDefinitionSubItemDefinitions;
    }

    private boolean isStructure(final DataType dataType) {
        return Objects.equals(dataType.getType(), dataTypeManager.structure());
    }
}
