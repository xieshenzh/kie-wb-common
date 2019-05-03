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

package org.kie.workbench.common.stunner.cm.client.command.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.kie.workbench.common.stunner.bpmn.definition.SequenceFlow;
import org.kie.workbench.common.stunner.bpmn.definition.StartNoneEvent;
import org.kie.workbench.common.stunner.cm.definition.AdHocSubprocess;
import org.kie.workbench.common.stunner.cm.definition.CaseManagementDiagram;
import org.kie.workbench.common.stunner.cm.definition.ReusableSubprocess;
import org.kie.workbench.common.stunner.cm.definition.UserTask;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class CaseManagementCommandUtil {

    @SuppressWarnings("unchecked")
    public static int getGraphChildIndex(final Node parent,
                                         final Node child) {
        if (parent != null && child != null) {
            List<Edge> outEdges = parent.getOutEdges();
            if (null != outEdges && !outEdges.isEmpty()) {
                for (int i = 0, n = outEdges.size(); i < n; i++) {
                    if (child.equals(outEdges.get(i).getTargetNode())) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    public static int getCanvasChildIndex(final Node<View<?>, Edge> parent,
                                          final Node<View<?>, Edge> child) {
        if (parent != null && child != null) {
            List<Edge> outEdges = parent.getOutEdges();
            if (null != outEdges && !outEdges.isEmpty()) {
                if (Objects.isNull(parent.getContent()) ||
                        !(parent.getContent().getDefinition() instanceof CaseManagementDiagram)) {
                    for (int i = 0, n = outEdges.size(); i < n; i++) {
                        if (child.equals(outEdges.get(i).getTargetNode())) {
                            return i;
                        }
                    }
                } else {
                    for (int i = 0, c = 0, n = outEdges.size(); i < n; i++) {
                        final Node cNode = outEdges.get(i).getTargetNode();
                        if (child.equals(cNode)) {
                            return c;
                        }

                        if (isStageNode(cNode)) {
                            c++;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public static boolean isStage(final Node<View<?>, Edge> parent, final Node<View<?>, Edge> child) {
        return !Objects.isNull(parent.getContent())
                && parent.getContent().getDefinition() instanceof CaseManagementDiagram
                && isStageNode(child);
    }

    public static boolean isStageNode(final Node<View<?>, Edge> node) {
        if (node.getContent().getDefinition() instanceof AdHocSubprocess) {
            final List<Node> childNodes = node.getOutEdges().stream()
                    .filter(childPredicate())
                    .map(Edge::getTargetNode).collect(Collectors.toList());

            if (childNodes.isEmpty()) {
                return true;
            }

            if (childNodes.stream().map(cNode -> ((View) cNode.getContent()).getDefinition())
                    .allMatch(def -> def instanceof UserTask || def instanceof ReusableSubprocess)) {
                return true;
            }
        }

        return false;
    }

    public static Predicate<Edge> childPredicate() {
        return edge -> edge.getContent() instanceof Child;
    }

    public static Predicate<Edge> sequencePredicate() {
        return edge -> edge.getContent() instanceof SequenceFlow;
    }

    @SuppressWarnings("unchecked")
    public static int getCanvasNewChildIndex(final Node<View<?>, Edge> parent) {
        if (Objects.isNull(parent.getContent()) ||
                !(parent.getContent().getDefinition() instanceof CaseManagementDiagram)) {
            return parent.getOutEdges().size();
        }

        return (int) parent.getOutEdges().stream()
                .filter(childPredicate())
                .filter(edge -> isStageNode(edge.getTargetNode())).count();
    }

    @SuppressWarnings("unchecked")
    public static int getGraphNewChildIndex(final Node<View<?>, Edge> parent) {
        if (Objects.isNull(parent.getContent()) ||
                !(parent.getContent().getDefinition() instanceof CaseManagementDiagram)) {
            return parent.getOutEdges().size();
        }

        List<Node<View<?>, Edge>> childNodes = parent.getOutEdges().stream()
                .map(edge -> (Node<View<?>, Edge>) edge.getTargetNode())
                .collect(Collectors.toList());

        for (int n = childNodes.size(), i = n - 1; i >= 0; i--) {
            Node<View<?>, Edge> node = childNodes.get(i);
            if (isStageNode(node)
                    || node.getContent().getDefinition() instanceof StartNoneEvent) {
                return i + 1;
            }
        }

        return 0;
    }
}
