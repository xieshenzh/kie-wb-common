/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package com.kie.workbench.common.stunner.cm.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.stunner.bpmn.BPMNDefinitionSet;
import org.kie.workbench.common.stunner.cm.CaseManagementDefinitionSet;
import org.kie.workbench.common.stunner.cm.project.service.CaseManagementSwitchViewService;
import org.kie.workbench.common.stunner.core.definition.service.DefinitionSetService;
import org.kie.workbench.common.stunner.core.diagram.AbstractMetadata;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;

@ApplicationScoped
@Service
public class CaseManagementSwitchViewServiceImpl implements CaseManagementSwitchViewService {

    private final Instance<DefinitionSetService> definitionSetServiceInstances;

    private Collection<DefinitionSetService> definitionSetServices;

    private final Map<String, String> definitionTransitionMapping;

    @Inject
    public CaseManagementSwitchViewServiceImpl(final Instance<DefinitionSetService> definitionSetServiceInstances) {
        this.definitionSetServiceInstances = definitionSetServiceInstances;

        this.definitionSetServices = new LinkedList<>();
        this.definitionTransitionMapping = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        definitionSetServiceInstances.forEach(i -> definitionSetServices.add(i));

        this.definitionTransitionMapping.put(CaseManagementDefinitionSet.class.getName(), BPMNDefinitionSet.class.getName());
        this.definitionTransitionMapping.put(BPMNDefinitionSet.class.getName(), CaseManagementDefinitionSet.class.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void switchView(Diagram diagram) {
        final Metadata metadata = diagram.getMetadata();
        final String defSetId = metadata.getDefinitionSetId();

        final Optional<DefinitionSetService> definitionSetServiceOptional =
                definitionSetServices.stream().filter(s -> s.accepts(defSetId)).findAny();

        definitionSetServiceOptional.ifPresent(service -> {
            try {
                final String rawData = service.getDiagramMarshaller().marshall(diagram);

                final String mappedDefSetId = definitionTransitionMapping.get(defSetId);

                final Optional<DefinitionSetService> mappedDefinitionSetServiceOptional = definitionSetServices.stream()
                        .filter(s -> s.accepts(mappedDefSetId)).findAny();

                mappedDefinitionSetServiceOptional.ifPresent(mappedService -> {
                    ((AbstractMetadata) metadata).setDefinitionSetId(mappedDefSetId);

                    try (final InputStream inputStream = new ByteArrayInputStream(rawData.getBytes())) {
                        final Graph<DefinitionSet, Node> graph = mappedService.getDiagramMarshaller().unmarshall(metadata, inputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}