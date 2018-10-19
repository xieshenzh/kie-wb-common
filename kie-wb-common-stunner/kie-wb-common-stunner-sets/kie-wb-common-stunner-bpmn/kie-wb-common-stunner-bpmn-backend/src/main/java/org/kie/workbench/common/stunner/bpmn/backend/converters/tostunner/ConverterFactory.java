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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner;

import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.activities.CallActivityConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.processes.SubProcessConverter;
import org.kie.workbench.common.stunner.bpmn.definition.AdHocSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.ReusableSubprocess;

public class ConverterFactory extends BaseConverterFactory<BPMNDiagramImpl, AdHocSubprocess, EmbeddedSubprocess, ReusableSubprocess> {

    public ConverterFactory(DefinitionResolver definitionResolver,
                            TypedFactoryManager factoryManager) {
        super(definitionResolver, factoryManager);
    }

    @Override
    public SubProcessConverter subProcessConverter() {
        return new SubProcessConverter(factoryManager,
                                       propertyReaderFactory,
                                       definitionResolver,
                                       this);
    }

    @Override
    protected CallActivityConverter createCallActivityConverter() {
        return new CallActivityConverter(factoryManager, propertyReaderFactory, this);
    }

    @Override
    public Class<BPMNDiagramImpl> getDiagramClass() {
        return BPMNDiagramImpl.class;
    }

    @Override
    public Class<ReusableSubprocess> getReusableSubprocessClass() {
        return ReusableSubprocess.class;
    }

    @Override
    public Class<AdHocSubprocess> getAdhocSubprocessClass() {
        return AdHocSubprocess.class;
    }

    @Override
    public Class<EmbeddedSubprocess> getEmbeddedSubprocessClass() {
        return EmbeddedSubprocess.class;
    }
}
