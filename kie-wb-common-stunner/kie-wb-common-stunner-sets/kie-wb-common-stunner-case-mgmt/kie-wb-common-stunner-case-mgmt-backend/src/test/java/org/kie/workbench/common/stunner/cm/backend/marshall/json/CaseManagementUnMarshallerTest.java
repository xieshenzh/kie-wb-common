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

package org.kie.workbench.common.stunner.cm.backend.marshall.json;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.MetaDataType;
import org.junit.Test;
import org.kie.workbench.common.stunner.bpmn.backend.marshall.json.builder.GraphObjectBuilderFactory;
import org.kie.workbench.common.stunner.bpmn.backend.marshall.json.oryx.OryxManager;
import org.kie.workbench.common.stunner.cm.definition.CaseManagementDiagram;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandManager;
import org.kie.workbench.common.stunner.core.graph.command.impl.GraphCommandFactory;
import org.kie.workbench.common.stunner.core.graph.processing.index.GraphIndexBuilder;
import org.kie.workbench.common.stunner.core.registry.impl.DefinitionsCacheRegistry;
import org.kie.workbench.common.stunner.core.rule.RuleManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CaseManagementUnMarshallerTest {

    private String expectedStencil;
    private CaseManagementUnMarshaller tested = new CaseManagementUnMarshaller(mock(GraphObjectBuilderFactory.class),
                                                                               mock(DefinitionManager.class),
                                                                               mock(FactoryManager.class),
                                                                               mock(DefinitionsCacheRegistry.class),
                                                                               mock(RuleManager.class),
                                                                               mock(OryxManager.class),
                                                                               mock(GraphCommandManager.class),
                                                                               mock(GraphCommandFactory.class),
                                                                               mock(GraphIndexBuilder.class),
                                                                               Object.class,
                                                                               CaseManagementDiagram.class) {

        @Override
        void doMarshallNode(FlowNode node,
                            Map<String, Object> properties,
                            String stencil,
                            BPMNPlane plane,
                            JsonGenerator generator,
                            float xOffset,
                            float yOffset) throws IOException {
            assertEquals(stencil, expectedStencil);
        }
    };

    @Test
    public void testMarshallNode_AdHocSubprocess() throws Exception {
        expectedStencil = "AdHocSubprocess";

        tested.marshallNode(mock(FlowNode.class),
                            new HashMap<>(),
                            "AdHocSubprocess",
                            mock(BPMNPlane.class),
                            mock(JsonGenerator.class),
                            0.0f,
                            0.0f);
    }

    @Test
    public void testMarshallNode_CaseReusableSubprocess() throws Exception {
        expectedStencil = "CaseReusableSubprocess";

        final MetaDataType metaDataType = mock(MetaDataType.class);
        when(metaDataType.getName()).thenReturn("case");
        when(metaDataType.getMetaValue()).thenReturn("true");

        final FeatureMap featureMap = mock(FeatureMap.class);
        when(featureMap.get(eq(DroolsPackage.Literals.DOCUMENT_ROOT__META_DATA), eq(true)))
                .thenReturn(Collections.singletonList(metaDataType));

        final ExtensionAttributeValue extensionAttributeValue = mock(ExtensionAttributeValue.class);
        when(extensionAttributeValue.getValue()).thenReturn(featureMap);

        final CallActivity callActivity = mock(CallActivity.class);
        when(callActivity.getExtensionValues()).thenReturn(Collections.singletonList(extensionAttributeValue));

        tested.marshallNode(callActivity,
                            new HashMap<>(),
                            "ReusableSubprocess",
                            mock(BPMNPlane.class),
                            mock(JsonGenerator.class),
                            0.0f,
                            0.0f);
    }

    @Test
    public void testMarshallNode_ProcessReusableSubprocess() throws Exception {
        expectedStencil = "ProcessReusableSubprocess";

        final MetaDataType metaDataType = mock(MetaDataType.class);
        when(metaDataType.getName()).thenReturn("case");
        when(metaDataType.getMetaValue()).thenReturn("false");

        final FeatureMap featureMap = mock(FeatureMap.class);
        when(featureMap.get(eq(DroolsPackage.Literals.DOCUMENT_ROOT__META_DATA), eq(true)))
                .thenReturn(Collections.singletonList(metaDataType));

        final ExtensionAttributeValue extensionAttributeValue = mock(ExtensionAttributeValue.class);
        when(extensionAttributeValue.getValue()).thenReturn(featureMap);

        final CallActivity callActivity = mock(CallActivity.class);
        when(callActivity.getExtensionValues()).thenReturn(Collections.singletonList(extensionAttributeValue));

        tested.marshallNode(callActivity,
                            new HashMap<>(),
                            "ReusableSubprocess",
                            mock(BPMNPlane.class),
                            mock(JsonGenerator.class),
                            0.0f,
                            0.0f);
    }
}