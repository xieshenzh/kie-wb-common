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

package org.kie.workbench.common.stunner.cm.client.presenters.session.impl;

import java.util.stream.StreamSupport;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.kie.workbench.common.stunner.client.widgets.event.SessionDiagramOpenedEvent;
import org.kie.workbench.common.stunner.client.widgets.event.SessionFocusedEvent;
import org.kie.workbench.common.stunner.client.widgets.notification.CommandNotification;
import org.kie.workbench.common.stunner.client.widgets.notification.NotificationsObserver;
import org.kie.workbench.common.stunner.client.widgets.palette.DefaultPaletteFactory;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.impl.SessionEditorImpl;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.impl.SessionEditorPresenter;
import org.kie.workbench.common.stunner.client.widgets.toolbar.impl.EditorToolbar;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.client.preferences.StunnerPreferencesRegistries;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;

@Dependent
@Alternative
public class CaseManagementSessionEditorPresenter<S extends EditorSession> extends SessionEditorPresenter<S> {

    @Inject
    private CaseManagementRuleViolationHideErrorPredicateEvaluator hideErrorPredicate;

    @Inject
    public CaseManagementSessionEditorPresenter(final DefinitionUtils definitionUtils,
                                                final SessionManager sessionManager,
                                                final SessionEditorImpl<S> editor,
                                                final Event<SessionDiagramOpenedEvent> sessionDiagramOpenedEvent,
                                                final @Any ManagedInstance<EditorToolbar> toolbars,
                                                final DefaultPaletteFactory<AbstractCanvasHandler> paletteWidgetFactory,
                                                final NotificationsObserver notificationsObserver,
                                                final Event<SessionFocusedEvent> sessionFocusedEvent,
                                                final StunnerPreferencesRegistries preferencesRegistries,
                                                final View view) {
        super(definitionUtils,
              sessionManager,
              editor,
              sessionDiagramOpenedEvent,
              toolbars,
              paletteWidgetFactory,
              notificationsObserver,
              sessionFocusedEvent,
              preferencesRegistries, view);
    }

    @Override
    protected void showCommandError(CommandNotification notification) {
        if (notification.getResult().map(this::isShowError).orElse(true)) {
            super.showCommandError(notification);
        }
    }

    private boolean isShowError(CommandResult<CanvasViolation> result) {
        return !StreamSupport.stream(result.getViolations().spliterator(), false)
                .allMatch(violation -> hideErrorPredicate.test(() -> violation.getRuleViolation().getClass(), violation::getRuleViolation));
    }
}
