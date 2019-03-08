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

package org.kie.workbench.common.stunner.cm.project.client.session.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.kie.workbench.common.stunner.cm.project.service.CaseManagementSwitchViewService;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.command.AbstractClientSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;

@Dependent
@Default
public class SwitchViewSessionCommand extends AbstractClientSessionCommand<EditorSession> {

    private static final Logger LOGGER = Logger.getLogger(SwitchViewSessionCommand.class.getName());

    private final Caller<CaseManagementSwitchViewService> caseManagementSwitchViewService;

    @Inject
    public SwitchViewSessionCommand(final Caller<CaseManagementSwitchViewService> caseManagementSwitchViewService) {
        super(true);

        this.caseManagementSwitchViewService = caseManagementSwitchViewService;
    }

    @Override
    public boolean accepts(ClientSession session) {
        return session instanceof EditorSession;
    }

    @Override
    public <V> void execute(Callback<V> callback) {
        try {
            caseManagementSwitchViewService.call().switchView(this.getCanvasHandler().getDiagram());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
