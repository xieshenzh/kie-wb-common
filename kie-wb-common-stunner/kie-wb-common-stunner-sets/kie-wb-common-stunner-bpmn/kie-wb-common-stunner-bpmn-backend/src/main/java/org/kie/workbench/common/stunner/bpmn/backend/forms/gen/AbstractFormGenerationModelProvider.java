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

package org.kie.workbench.common.stunner.bpmn.backend.forms.gen;

import java.io.IOException;

import org.eclipse.bpmn2.Definitions;
import org.kie.workbench.common.stunner.bpmn.backend.BaseDiagramMarshaller;
import org.kie.workbench.common.stunner.bpmn.backend.BaseDirectDiagramMarshaller;
import org.kie.workbench.common.stunner.core.backend.service.AbstractDefinitionSetService;
import org.kie.workbench.common.stunner.core.definition.service.DiagramMarshaller;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;
import org.kie.workbench.common.stunner.forms.backend.gen.FormGenerationModelProvider;

public abstract class AbstractFormGenerationModelProvider
        implements FormGenerationModelProvider<Definitions> {

    private AbstractDefinitionSetService backendService;
    private final DefinitionUtils definitionUtils;
    private String definitionSetId;

    public AbstractFormGenerationModelProvider(final AbstractDefinitionSetService backendService,
                                               final DefinitionUtils definitionUtils) {
        this.backendService = backendService;
        this.definitionUtils = definitionUtils;
    }

    protected void init() {
        this.definitionSetId = definitionUtils.getDefinitionSetId(getDefinitionSetClass());
    }

    protected abstract Class<?> getDefinitionSetClass();

    @Override
    public boolean accepts(final Diagram diagram) {
        return this.definitionSetId.equals(diagram.getMetadata().getDefinitionSetId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Definitions generate(final Diagram diagram) throws IOException {
        DiagramMarshaller diagramMarshaller = backendService.getDiagramMarshaller();
        if (diagramMarshaller instanceof BaseDiagramMarshaller) {
            return (Definitions) ((BaseDiagramMarshaller) diagramMarshaller).marshallToBpmn2Resource(diagram).getContents().get(0);
        } else if (diagramMarshaller instanceof BaseDirectDiagramMarshaller) {
            return ((BaseDirectDiagramMarshaller) diagramMarshaller).marshallToBpmn2Definitions(diagram);
        }
        throw new IOException("Unexpected diagram marshaller type: " + diagramMarshaller.getMetadataMarshaller().getClass());
    }
}
