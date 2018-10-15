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
package org.kie.workbench.common.stunner.forms.client.widgets;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.CanvasSelectionEvent;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.DomainObjectSelectionEvent;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandFactory;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.domainobject.DomainObject;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.processing.index.Index;
import org.kie.workbench.common.stunner.forms.client.event.RefreshFormPropertiesEvent;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.mvp.Command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormsCanvasSessionHandlerTest {

    private static final String UUID = "uuid";

    @Mock
    private EditorSession session;

    @Mock
    private DefinitionManager definitionManager;

    @Mock
    private AbstractCanvasHandler abstractCanvasHandler;

    @Mock
    private CanvasCommandFactory<AbstractCanvasHandler> commandFactory;

    @Mock
    private FormsCanvasSessionHandler.FormRenderer formRenderer;

    @Mock
    private Index<?, ?> index;

    @Mock
    private Diagram<?, ?> diagram;

    @Mock
    private Graph graph;

    @Mock
    private Element<? extends Definition<?>> element;

    @Mock
    private DomainObject domainObject;

    private RefreshFormPropertiesEvent refreshFormPropertiesEvent;

    private CanvasSelectionEvent canvasSelectionEvent;

    private DomainObjectSelectionEvent domainObjectSelectionEvent;

    private FormsCanvasSessionHandler handler;

    @Before
    public void setup() {
        this.refreshFormPropertiesEvent = new RefreshFormPropertiesEvent(session, UUID);
        this.handler = spy(new FormsCanvasSessionHandler(definitionManager, commandFactory));
        this.handler.setRenderer(formRenderer);

        when(session.getCanvasHandler()).thenReturn(abstractCanvasHandler);
        when(abstractCanvasHandler.getGraphIndex()).thenReturn(index);
        when(abstractCanvasHandler.getDiagram()).thenReturn(diagram);
        when(index.get(eq(UUID))).thenReturn(element);
        when(diagram.getGraph()).thenReturn(graph);
    }

    @Test
    public void testOnRefreshFormPropertiesEventSameSession() {
        handler.bind(session);

        handler.onRefreshFormPropertiesEvent(refreshFormPropertiesEvent);

        verify(formRenderer).render(anyString(), eq(element), any(Command.class));
    }

    @Test
    public void testOnRefreshFormPropertiesEventDifferentSession() {
        handler.bind(mock(EditorSession.class));

        handler.onRefreshFormPropertiesEvent(refreshFormPropertiesEvent);

        verify(formRenderer, never()).render(anyString(), any(Element.class), any(Command.class));
    }

    @Test
    public void testOnCanvasSelectionEventSameSession() {
        handler.bind(session);

        canvasSelectionEvent = new CanvasSelectionEvent(abstractCanvasHandler, UUID);

        handler.onCanvasSelectionEvent(canvasSelectionEvent);

        verify(formRenderer).render(anyString(), eq(element), any(Command.class));
    }

    @Test
    public void testOnCanvasSelectionEventSameSessionMultipleNodes() {
        handler.bind(mock(EditorSession.class));

        canvasSelectionEvent = new CanvasSelectionEvent(abstractCanvasHandler, Arrays.asList(new String[]{UUID, UUID}));

        handler.onCanvasSelectionEvent(canvasSelectionEvent);

        verify(formRenderer, never()).render(anyString(), any(Element.class), any(Command.class));
    }

    @Test
    public void testOnCanvasSelectionEventDifferentSession() {
        handler.bind(mock(EditorSession.class));

        canvasSelectionEvent = new CanvasSelectionEvent(abstractCanvasHandler, UUID);

        handler.onCanvasSelectionEvent(canvasSelectionEvent);

        verify(formRenderer, never()).render(anyString(), any(DomainObject.class));
    }

    @Test
    public void testOnDomainObjectSelectionEventSameSession() {
        handler.bind(session);

        domainObjectSelectionEvent = new DomainObjectSelectionEvent(abstractCanvasHandler, domainObject);

        handler.onDomainObjectSelectionEvent(domainObjectSelectionEvent);

        verify(formRenderer).render(anyString(), eq(domainObject));
    }

    @Test
    public void testOnDomainObjectSelectionEventDifferentSession() {
        handler.bind(mock(EditorSession.class));

        domainObjectSelectionEvent = new DomainObjectSelectionEvent(abstractCanvasHandler, domainObject);

        handler.onDomainObjectSelectionEvent(domainObjectSelectionEvent);

        verify(formRenderer, never()).render(anyString(), any(Element.class), any(Command.class));
    }
}
