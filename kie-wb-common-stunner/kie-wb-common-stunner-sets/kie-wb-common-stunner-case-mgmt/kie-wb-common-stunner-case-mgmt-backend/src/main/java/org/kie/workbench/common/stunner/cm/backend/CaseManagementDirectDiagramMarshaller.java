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
package org.kie.workbench.common.stunner.cm.backend;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.bpmn.backend.BaseDirectDiagramMarshaller;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.DefinitionsBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.PropertyWriterFactory;
import org.kie.workbench.common.stunner.bpmn.backend.workitem.service.WorkItemDefinitionBackendService;
import org.kie.workbench.common.stunner.cm.backend.converters.fromstunner.CaseManagementConverterFactory;
import org.kie.workbench.common.stunner.cm.definition.CaseManagementDiagram;
import org.kie.workbench.common.stunner.cm.qualifiers.CaseManagementEditor;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.backend.service.XMLEncoderDiagramMetadataMarshaller;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandManager;
import org.kie.workbench.common.stunner.core.graph.command.impl.GraphCommandFactory;
import org.kie.workbench.common.stunner.core.rule.RuleManager;

@Dependent
@CaseManagementEditor
public class CaseManagementDirectDiagramMarshaller extends BaseDirectDiagramMarshaller {

    @Inject
    public CaseManagementDirectDiagramMarshaller(
            final XMLEncoderDiagramMetadataMarshaller diagramMetadataMarshaller,
            final DefinitionManager definitionManager,
            final RuleManager ruleManager,
            final WorkItemDefinitionBackendService workItemDefinitionService,
            final FactoryManager factoryManager,
            final GraphCommandFactory commandFactory,
            final GraphCommandManager commandManager) {

        super(diagramMetadataMarshaller,
              definitionManager,
              ruleManager,
              workItemDefinitionService,
              factoryManager,
              commandFactory,
              commandManager);
    }

    @Override
    protected org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.BaseConverterFactory createFromStunnerConverterFactory(
            final Graph graph,
            final PropertyWriterFactory propertyWriterFactory) {
        return new CaseManagementConverterFactory(new DefinitionsBuildingContext(graph, CaseManagementDiagram.class),
                                                  propertyWriterFactory);
    }

    @Override
    protected org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.ConverterFactory createToStunnerConverterFactory() {
        return null;
    }
}
