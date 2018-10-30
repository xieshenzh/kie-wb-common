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

package org.kie.workbench.common.stunner.cm.project.client.type;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import org.guvnor.common.services.project.categories.Process;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.stunner.cm.client.resources.CaseManagementImageResources;
import org.kie.workbench.common.stunner.cm.project.client.resources.i18n.CaseManagementProjectClientConstants;
import org.kie.workbench.common.stunner.cm.resource.CaseManagementDefinitionSetResourceType;
import org.kie.workbench.common.stunner.project.client.type.AbstractStunnerClientResourceType;
import org.uberfire.experimental.definition.annotations.ExperimentalFeature;

import static org.kie.workbench.common.stunner.cm.project.client.resources.i18n.CaseManagementProjectClientConstants.CaseManagementDiagramResourceTypeDescription;
import static org.kie.workbench.common.stunner.cm.project.client.resources.i18n.CaseManagementProjectClientConstants.CaseManagementDiagramResourceTypeShortName;

@ApplicationScoped
@ExperimentalFeature(scope = ExperimentalFeature.Scope.GLOBAL,
        nameI18nKey = CaseManagementDiagramResourceTypeShortName,
        descriptionI18nKey = CaseManagementDiagramResourceTypeDescription)
public class CaseManagementDiagramResourceType extends AbstractStunnerClientResourceType<CaseManagementDefinitionSetResourceType> {

    private static final Image ICON = new Image(CaseManagementImageResources.INSTANCE.cmicon());

    private final TranslationService translationService;

    protected CaseManagementDiagramResourceType() {
        this(null, null, null);
    }

    @Inject
    public CaseManagementDiagramResourceType(final CaseManagementDefinitionSetResourceType definitionSetResourceType,
                                             final Process category,
                                             final TranslationService translationService) {
        super(definitionSetResourceType, category);
        this.translationService = translationService;
    }

    @Override
    public IsWidget getIcon() {
        return ICON;
    }

    @Override
    protected String getTranslatedShortName() {
        return translationService.getTranslation(CaseManagementProjectClientConstants.CaseManagementDiagramResourceTypeShortName);
    }

    @Override
    protected String getTranslatedDescription() {
        return translationService.getTranslation(CaseManagementProjectClientConstants.CaseManagementDiagramResourceTypeDescription);
    }
}
