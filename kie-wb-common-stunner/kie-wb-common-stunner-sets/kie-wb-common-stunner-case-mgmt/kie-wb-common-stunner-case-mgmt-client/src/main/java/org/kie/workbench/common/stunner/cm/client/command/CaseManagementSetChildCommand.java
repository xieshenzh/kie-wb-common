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

package org.kie.workbench.common.stunner.cm.client.command;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import org.kie.workbench.common.stunner.bpmn.definition.StartNoneEvent;
import org.kie.workbench.common.stunner.cm.client.command.canvas.CaseManagementSetChildNodeCanvasCommand;
import org.kie.workbench.common.stunner.cm.client.command.graph.CaseManagementSetChildNodeGraphCommand;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.command.AbstractCanvasCommand;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.rule.RuleViolation;

import static org.kie.workbench.common.stunner.cm.client.command.util.CaseManagementCommandUtil.getCanvasNewChildIndex;
import static org.kie.workbench.common.stunner.cm.client.command.util.CaseManagementCommandUtil.getGraphChildIndex;
import static org.kie.workbench.common.stunner.cm.client.command.util.CaseManagementCommandUtil.isStage;

public class CaseManagementSetChildCommand extends org.kie.workbench.common.stunner.core.client.canvas.command.SetChildrenCommand {

    protected final OptionalInt canvasIndex;
    protected final Optional<Node<View<?>, Edge>> originalParent;
    protected final OptionalInt originaCanvaslIndex;
    protected final Optional<Node<View<?>, Edge>> originalSibling;

    public CaseManagementSetChildCommand(final Node<View<?>, Edge> parent,
                                         final Node<View<?>, Edge> child) {
        this(parent,
             child,
             OptionalInt.of(getCanvasNewChildIndex(parent)),
             Optional.empty(),
             OptionalInt.empty(),
             Optional.empty());
    }

    public CaseManagementSetChildCommand(final Node<View<?>, Edge> parent,
                                         final Node<View<?>, Edge> child,
                                         final OptionalInt canvasIndex,
                                         final Optional<Node<View<?>, Edge>> originalParent,
                                         final OptionalInt originaCanvaslIndex,
                                         final Optional<Node<View<?>, Edge>> originalSibling) {
        super(parent,
              Collections.singleton(child));
        this.canvasIndex = canvasIndex;
        this.originalParent = originalParent;
        this.originaCanvaslIndex = originaCanvaslIndex;
        this.originalSibling = originalSibling;
    }

    public Node getCandidate() {
        return getCandidates().iterator().next();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Command<GraphCommandExecutionContext, RuleViolation> newGraphCommand(final AbstractCanvasHandler context) {
        OptionalInt originalIndex = originalParent
                .map(p -> isStage(parent, getCandidate())
                        ? OptionalInt.of(getGraphChildIndex(p, getCandidate()))
                        : originaCanvaslIndex)
                .orElseGet(OptionalInt::empty);

        OptionalInt index = originalSibling.map(s -> {
            List<Node<View<?>, Edge>> childNodes = ((Node<View<?>, Edge>) parent).getOutEdges().stream()
                    .map(e -> (Node<View<?>, Edge>) e.getTargetNode()).collect(Collectors.toList());
            for (int i = 0, n = childNodes.size(); i < n; i++) {
                if (s.equals(childNodes.get(i))) {
                    return OptionalInt.of(i);
                }
            }
            return OptionalInt.empty();
        }).orElseGet(() -> {
            if (canvasIndex.isPresent() && canvasIndex.getAsInt() == 0) {
                List<Node<View<?>, Edge>> childNodes = ((Node<View<?>, Edge>) parent).getOutEdges().stream()
                        .map(e -> (Node<View<?>, Edge>) e.getTargetNode()).collect(Collectors.toList());
                int i = 0;
                for (int n = childNodes.size(); i < n; i++) {
                    if (childNodes.get(i) instanceof StartNoneEvent) {
                        OptionalInt.of(++i);
                    }
                }
                return OptionalInt.of(0);
            } else {
                return OptionalInt.empty();
            }
        });

        return new CaseManagementSetChildNodeGraphCommand(parent,
                                                          getCandidate(),
                                                          index,
                                                          originalParent,
                                                          originalIndex);
    }

    @Override
    protected AbstractCanvasCommand newCanvasCommand(final AbstractCanvasHandler context) {
        return new CaseManagementSetChildNodeCanvasCommand(parent,
                                                           getCandidate(),
                                                           canvasIndex,
                                                           originalParent,
                                                           originaCanvaslIndex);
    }
}
