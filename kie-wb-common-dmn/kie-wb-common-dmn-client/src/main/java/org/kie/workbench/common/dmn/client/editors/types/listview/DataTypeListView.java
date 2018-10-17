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

package org.kie.workbench.common.dmn.client.editors.types.listview;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.NodeList;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.dmn.client.editors.types.common.DataType;
import org.kie.workbench.common.dmn.client.editors.types.listview.common.ElementHelper;

import static org.kie.workbench.common.dmn.client.editors.types.common.HiddenHelper.hide;
import static org.kie.workbench.common.dmn.client.editors.types.common.HiddenHelper.isHidden;
import static org.kie.workbench.common.dmn.client.editors.types.common.HiddenHelper.show;
import static org.kie.workbench.common.dmn.client.editors.types.common.JQuery.$;
import static org.kie.workbench.common.dmn.client.editors.types.listview.DataTypeListItemView.ARROW_BUTTON_SELECTOR;
import static org.kie.workbench.common.dmn.client.editors.types.listview.DataTypeListItemView.PARENT_UUID_ATTR;
import static org.kie.workbench.common.dmn.client.editors.types.listview.DataTypeListItemView.UUID_ATTR;
import static org.kie.workbench.common.dmn.client.editors.types.listview.common.ElementHelper.remove;
import static org.kie.workbench.common.dmn.client.editors.types.listview.common.ListItemViewCssHelper.isRightArrow;

@Templated
@ApplicationScoped
public class DataTypeListView implements DataTypeList.View {

    @DataField("list-items")
    private final HTMLDivElement listItems;

    @DataField("collapsed-description")
    private final HTMLDivElement collapsedDescription;

    @DataField("expanded-description")
    private final HTMLDivElement expandedDescription;

    @DataField("view-more")
    private final HTMLAnchorElement viewMore;

    @DataField("view-less")
    private final HTMLAnchorElement viewLess;

    @DataField("add-button")
    private final HTMLButtonElement addButton;

    private DataTypeList presenter;

    @Inject
    public DataTypeListView(final HTMLDivElement listItems,
                            final HTMLDivElement collapsedDescription,
                            final HTMLDivElement expandedDescription,
                            final HTMLAnchorElement viewMore,
                            final HTMLAnchorElement viewLess,
                            final HTMLButtonElement addButton) {
        this.listItems = listItems;
        this.collapsedDescription = collapsedDescription;
        this.expandedDescription = expandedDescription;
        this.viewMore = viewMore;
        this.viewLess = viewLess;
        this.addButton = addButton;
    }

    @Override
    public void init(final DataTypeList presenter) {
        this.presenter = presenter;
    }

    @PostConstruct
    public void setup() {
        collapseDescription();
    }

    @Override
    public void setupListItems(final List<DataTypeListItem> listItems) {
        this.listItems.innerHTML = "";
        listItems.forEach(this::appendItem);
    }

    @Override
    public void addSubItems(final DataType dataType,
                            final List<DataTypeListItem> listItems) {

        cleanSubTypes(dataType.getUUID());

        Element parent = getDataTypeRow(dataType);

        for (final DataTypeListItem item : listItems) {

            final HTMLElement itemElement = item.getElement();

            hideItemElementIfParentIsCollapsed(itemElement, parent);

            ElementHelper.insertAfter(itemElement, parent);
            parent = itemElement;
        }

        showArrowIconIfDataTypeHasChildren(dataType);
    }

    @Override
    public void addSubItem(final DataTypeListItem listItem) {
        listItems.appendChild(listItem.getElement());
    }

    @EventHandler("add-button")
    public void onAddClick(final ClickEvent e) {

        final double scrollTop = listItems.scrollHeight;

        scrollTo(listItems, scrollTop);

        presenter.addDataType();
    }

    void hideItemElementIfParentIsCollapsed(final HTMLElement itemElement,
                                            final Element parent) {

        final boolean isCollapsedParent = isCollapsed(parent.querySelector(ARROW_BUTTON_SELECTOR));
        final boolean isHiddenParent = isHidden(parent);

        if (isCollapsedParent || isHiddenParent) {
            hide(itemElement);
        } else {
            show(itemElement);
        }
    }

    void showArrowIconIfDataTypeHasChildren(final DataType dataType) {
        if (hasChildren(dataType)) {
            show(getDataTypeRow(dataType).querySelector(ARROW_BUTTON_SELECTOR));
        } else {
            hide(getDataTypeRow(dataType).querySelector(ARROW_BUTTON_SELECTOR));
        }
    }

    private boolean hasChildren(final DataType dataType) {
        return listItems.querySelectorAll("[" + PARENT_UUID_ATTR + "=\"" + dataType.getUUID() + "\"]").length > 0;
    }

    @Override
    public void removeItem(final DataType dataType) {

        cleanSubTypes(dataType.getUUID());

        final Optional<Element> dataTypeRow = Optional.ofNullable(getDataTypeRow(dataType));

        dataTypeRow.ifPresent(ElementHelper::remove);
    }

    void cleanSubTypes(final String uuid) {

        final String selector = "[" + PARENT_UUID_ATTR + "=\"" + uuid + "\"]";
        final NodeList<Element> subDataTypeRows = listItems.querySelectorAll(selector);

        for (int i = 0; i < subDataTypeRows.length; i++) {
            final Element item = subDataTypeRows.getAt(i);
            if (item != null && item.parentNode != null) {
                cleanSubTypes(item.getAttribute(UUID_ATTR));
                remove(item);
            }
        }
    }

    @Override
    public void insertBelow(final DataTypeListItem listItem,
                            final DataType reference) {

        final Element elementReference = getLastSubDataTypeElement(reference);
        ElementHelper.insertAfter(listItem.getElement(), elementReference);
    }

    @Override
    public void insertAbove(final DataTypeListItem listItem,
                            final DataType reference) {

        final Element elementReference = getDataTypeRow(reference);
        ElementHelper.insertBefore(listItem.getElement(), elementReference);
    }

    boolean isCollapsed(final Element arrow) {
        return isRightArrow(arrow);
    }

    @EventHandler("view-more")
    public void onClickViewMore(final ClickEvent event) {
        expandDescription();
    }

    @EventHandler("view-less")
    public void onClickViewLess(final ClickEvent event) {
        collapseDescription();
    }

    private void appendItem(final DataTypeListItem listItem) {
        listItems.appendChild(listItem.getElement());
    }

    void expandDescription() {
        collapsedDescription.hidden = true;
        expandedDescription.hidden = false;
        viewLess.hidden = false;
        viewMore.hidden = true;
    }

    void collapseDescription() {
        collapsedDescription.hidden = false;
        expandedDescription.hidden = true;
        viewLess.hidden = true;
        viewMore.hidden = false;
    }

    Element getDataTypeRow(final DataType dataType) {
        return listItems.querySelector("[" + UUID_ATTR + "=\"" + dataType.getUUID() + "\"]");
    }

    Element getLastSubDataTypeElement(final DataType reference) {
        return getLastSubDataTypeElement(getDataTypeRow(reference));
    }

    Element getLastSubDataTypeElement(final Element element) {

        final String parentUUID = element.getAttribute(UUID_ATTR);
        final String selector = "[" + PARENT_UUID_ATTR + "=\"" + parentUUID + "\"]";
        final NodeList<Element> nestedElements = listItems.querySelectorAll(selector);

        if (nestedElements.length == 0) {
            return element;
        } else {
            return getLastSubDataTypeElement(nestedElements.getAt((int) nestedElements.length - 1));
        }
    }

    void scrollTo(final HTMLDivElement listItems,
                  final double scrollPosition) {

        final JavaScriptObject scrollTopProperty = property("scrollTop", scrollPosition);
        final int duration = 800;

        $(listItems).animate(scrollTopProperty, duration);
    }

    private JavaScriptObject property(final String key,
                                      final double value) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(key, new JSONNumber(value));
        return jsonObject.getJavaScriptObject();
    }
}
