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

package org.kie.workbench.common.dmn.client.editors.types;

import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.Node;
import org.kie.workbench.common.dmn.api.definition.v1_1.ItemDefinition;
import org.kie.workbench.common.dmn.client.editors.types.common.DataType;
import org.kie.workbench.common.dmn.client.editors.types.common.DataTypeManager;
import org.kie.workbench.common.dmn.client.editors.types.common.DataTypeManagerStackStore;
import org.kie.workbench.common.dmn.client.editors.types.common.ItemDefinitionUtils;
import org.kie.workbench.common.dmn.client.editors.types.common.JQueryEvent;
import org.kie.workbench.common.dmn.client.editors.types.listview.DataTypeList;
import org.kie.workbench.common.dmn.client.editors.types.messages.DataTypeFlashMessages;
import org.kie.workbench.common.dmn.client.editors.types.persistence.DataTypeStore;
import org.kie.workbench.common.dmn.client.editors.types.persistence.ItemDefinitionStore;
import org.uberfire.ext.editor.commons.client.file.popups.elemental2.Elemental2Modal;

import static org.kie.workbench.common.dmn.client.editors.types.common.JQuery.$;

@ApplicationScoped
public class DataTypeModal extends Elemental2Modal<DataTypeModal.View> {

    private final DataTypeList treeList;

    private final ItemDefinitionUtils itemDefinitionUtils;

    private final ItemDefinitionStore definitionStore;

    private final DataTypeStore dataTypeStore;

    private final DataTypeManager dataTypeManager;

    private final DataTypeManagerStackStore stackIndex;

    private final DataTypeFlashMessages flashMessages;

    @Inject
    public DataTypeModal(final View view,
                         final DataTypeList treeList,
                         final ItemDefinitionUtils itemDefinitionUtils,
                         final ItemDefinitionStore definitionStore,
                         final DataTypeStore dataTypeStore,
                         final DataTypeManager dataTypeManager,
                         final DataTypeManagerStackStore stackIndex,
                         final DataTypeFlashMessages flashMessages) {
        super(view);

        this.treeList = treeList;
        this.itemDefinitionUtils = itemDefinitionUtils;
        this.definitionStore = definitionStore;
        this.dataTypeStore = dataTypeStore;
        this.dataTypeManager = dataTypeManager;
        this.stackIndex = stackIndex;
        this.flashMessages = flashMessages;
    }

    @PostConstruct
    public void setup() {
        super.setup();
        setDataTypeModalCSSClasses();
        getView().setup(flashMessages, treeList);
    }

    public void show() {
        setupOnCloseCallback();
        cleanDataTypeStore();
        loadDataTypes();
        superShow();
    }

    void cleanDataTypeStore() {
        definitionStore.clear();
        dataTypeStore.clear();
        stackIndex.clear();
    }

    void loadDataTypes() {
        treeList.setupItems(itemDefinitionUtils
                                    .all()
                                    .stream()
                                    .map(this::makeDataType)
                                    .collect(Collectors.toList()));
    }

    DataType makeDataType(final ItemDefinition itemDefinition) {
        return dataTypeManager.from(itemDefinition).get();
    }

    void superShow() {
        super.show();
    }

    void setDataTypeModalCSSClasses() {
        final Element modalDialogElement = getModalDialogElement();
        modalDialogElement.classList.add("kie-data-types-modal");
    }

    void onCloseEvent(final JQueryEvent event) {
        flashMessages.hideMessages();
    }

    Element getModalDialogElement() {
        final HTMLElement body = getView().getBody();
        final Node modalBodyNode = body.parentNode;
        final Node modalContentNode = modalBodyNode.parentNode;
        final Node modalDialogNode = modalContentNode.parentNode;
        final Node modalParentNode = modalDialogNode.parentNode;
        return modalParentNode.querySelector(".modal-dialog");
    }

    void setupOnCloseCallback() {
        $(getModalDialogElement().parentNode).on("hidden.bs.modal", this::onCloseEvent);
    }

    public interface View extends Elemental2Modal.View<DataTypeModal> {

        void setup(final DataTypeFlashMessages flashMessages,
                   final DataTypeList treeGrid);
    }
}
