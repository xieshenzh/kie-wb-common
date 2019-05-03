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

package org.kie.workbench.common.stunner.cm.client.command.graph;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.kie.workbench.common.stunner.bpmn.definition.EndNoneEvent;
import org.kie.workbench.common.stunner.bpmn.definition.SequenceFlow;
import org.kie.workbench.common.stunner.bpmn.definition.StartNoneEvent;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandResultBuilder;
import org.kie.workbench.common.stunner.core.graph.command.impl.AbstractGraphCommand;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.impl.EdgeImpl;
import org.kie.workbench.common.stunner.core.rule.RuleViolation;
import org.kie.workbench.common.stunner.core.util.UUID;

import static org.kie.workbench.common.stunner.cm.client.command.util.CaseManagementCommandUtil.childPredicate;
import static org.kie.workbench.common.stunner.cm.client.command.util.CaseManagementCommandUtil.isStage;
import static org.kie.workbench.common.stunner.cm.client.command.util.CaseManagementCommandUtil.isStageNode;
import static org.kie.workbench.common.stunner.cm.client.command.util.CaseManagementCommandUtil.sequencePredicate;

public class CaseManagementSetChildNodeGraphCommand extends AbstractGraphCommand {

    protected final OptionalInt index;
    protected final Optional<Node<View<?>, Edge>> originalParent;
    protected final OptionalInt originalIndex;
    private final Node<View<?>, Edge> parent;
    private final Node<View<?>, Edge> child;

    private Optional<Node<View<?>, Edge>> in;
    private Optional<Node<View<?>, Edge>> out;
    private Optional<Edge> edge;
    private Optional<Node<View<?>, Edge>> originalIn;
    private Optional<Node<View<?>, Edge>> originalOut;

    public CaseManagementSetChildNodeGraphCommand(final Node<View<?>, Edge> parent,
                                                  final Node<View<?>, Edge> child,
                                                  final OptionalInt index,
                                                  final Optional<Node<View<?>, Edge>> originalParent,
                                                  final OptionalInt originalIndex) {
        this.parent = parent;
        this.child = child;
        this.index = index;
        this.originalParent = originalParent;
        this.originalIndex = originalIndex;

        this.in = Optional.empty();
        this.out = Optional.empty();
        this.edge = Optional.empty();
        this.originalIn = Optional.empty();
        this.originalOut = Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommandResult<RuleViolation> execute(final GraphCommandExecutionContext context) {
        final CommandResult<RuleViolation> results = allow(context);
        if (results.getType().equals(CommandResult.Type.ERROR)) {
            return results;
        }

        removeExistingRelationship(context);
        addNewRelationship(context);

        return results;
    }

    @SuppressWarnings("unchecked")
    private void removeExistingRelationship(final GraphCommandExecutionContext context) {
        originalParent.ifPresent(p -> {
            if (isStage(p, child)) {
                originalIn = child.getInEdges().stream()
                        .filter(sequencePredicate()).map(e -> (Node<View<?>, Edge>) e.getSourceNode()).findAny();
                originalOut = child.getOutEdges().stream()
                        .filter(sequencePredicate()).map(e -> (Node<View<?>, Edge>) e.getTargetNode()).findAny();
            }
        });

        //Remove existing relationship
        getEdgeForTarget(originalParent, child, childPredicate())
                .ifPresent((e) -> removeRelationship(e,
                                                     originalParent.get(),
                                                     child,
                                                     context));
        getEdgeForTarget(originalIn, child, sequencePredicate())
                .ifPresent((e) -> removeRelationship(e,
                                                     originalIn.get(),
                                                     child,
                                                     context));
        getEdgeForSource(originalOut, child, sequencePredicate())
                .ifPresent((e) -> removeRelationship(e,
                                                     child,
                                                     originalOut.get(),
                                                     context));

        if (originalIn.isPresent() && originalOut.isPresent()) {
            addRelationship(originalIn.get(),
                            originalOut.get(),
                            OptionalInt.empty(),
                            context,
                            SequenceFlow::new);
        }
    }

    @SuppressWarnings("unchecked")
    private void addNewRelationship(final GraphCommandExecutionContext context) {
        if (isStage(parent, child)) {
            List<Node> childNodes = parent.getOutEdges().stream()
                    .map(Edge::getTargetNode).collect(Collectors.toList());

            if (index.isPresent()) {
                for (int i = index.getAsInt() - 1; i >= 0; i--) {
                    Node node = childNodes.get(i);
                    if (isStageNode(node)
                            || ((Node<View<?>, Edge>) node).getContent().getDefinition() instanceof StartNoneEvent) {
                        in = Optional.of(node);
                        break;
                    }
                }

                for (int i = index.getAsInt(), n = childNodes.size(); i < n; i++) {
                    Node node = childNodes.get(i);
                    if (isStageNode(node)
                            || ((Node<View<?>, Edge>) node).getContent().getDefinition() instanceof EndNoneEvent) {
                        out = Optional.of(node);
                        break;
                    }
                }
            } else {
                int n = childNodes.size();
                int i = n - 1;
                for (; i >= 0; i--) {
                    Node node = childNodes.get(i);
                    if (isStageNode(node)
                            || ((Node<View<?>, Edge>) node).getContent().getDefinition() instanceof StartNoneEvent) {
                        in = Optional.of(node);
                        break;
                    }
                }

                if (i >= 0) {
                    for (; i < n; i++) {
                        Node node = childNodes.get(i);
                        if (((Node<View<?>, Edge>) node).getContent().getDefinition() instanceof EndNoneEvent) {
                            out = Optional.of(node);
                            break;
                        }
                    }
                }
            }
        }

        if (in.isPresent() && out.isPresent()) {
            edge = getEdgeForTarget(in,
                                    out.get(),
                                    sequencePredicate());
        }

        edge.ifPresent(e -> removeRelationship(e,
                                               in.get(),
                                               out.get(),
                                               context));

        //Add new relationship
        addRelationship(parent,
                        child,
                        index,
                        context,
                        Child::new);
        in.ifPresent(n -> addRelationship(n,
                                          child,
                                          OptionalInt.empty(),
                                          context,
                                          SequenceFlow::new));
        out.ifPresent(n -> addRelationship(child,
                                           n,
                                           OptionalInt.empty(),
                                           context,
                                           SequenceFlow::new));
    }

    @SuppressWarnings("unchecked")
    private Optional<Edge> getEdgeForTarget(final Optional<Node<View<?>, Edge>> sourceNode,
                                            final Node<View<?>, Edge> targetNode,
                                            final Predicate<Edge> relationship) {
        return sourceNode.flatMap(sn -> sn.getOutEdges().stream()
                .filter(relationship).filter(e -> targetNode.equals(e.getTargetNode())).findAny());
    }

    @SuppressWarnings("unchecked")
    private Optional<Edge> getEdgeForSource(final Optional<Node<View<?>, Edge>> targetNode,
                                            final Node<View<?>, Edge> sourceNode,
                                            final Predicate<Edge> relationship) {
        return targetNode.flatMap(tn -> tn.getInEdges().stream()
                .filter(relationship).filter(e -> sourceNode.equals(e.getSourceNode())).findAny());
    }

    @SuppressWarnings("unchecked")
    private void removeRelationship(final Edge e,
                                    final Node parent,
                                    final Node child,
                                    final GraphCommandExecutionContext context) {
        e.setSourceNode(null);
        e.setTargetNode(null);
        parent.getOutEdges().remove(e);
        child.getInEdges().remove(e);
        getMutableIndex(context).removeEdge(e);
    }

    @SuppressWarnings("unchecked")
    private <C> void addRelationship(final Node<View<?>, Edge> sourceNode,
                                     final Node<View<?>, Edge> targetNode,
                                     final OptionalInt index,
                                     final GraphCommandExecutionContext context,
                                     final Supplier<C> relationship) {
        final String uuid = UUID.uuid();
        final Edge<C, Node> edge = new EdgeImpl<>(uuid);
        edge.setContent(relationship.get());
        edge.setSourceNode(sourceNode);
        edge.setTargetNode(targetNode);
        sourceNode.getOutEdges().add(index.orElseGet(() -> sourceNode.getOutEdges().size()), edge);
        targetNode.getInEdges().add(edge);
        getMutableIndex(context).addEdge(edge);
    }

    @SuppressWarnings("unchecked")
    protected CommandResult<RuleViolation> check(final GraphCommandExecutionContext context) {
        final Collection<RuleViolation> violations = evaluate(context,
                                                              contextBuilder -> contextBuilder.containment(parent,
                                                                                                           child));
        return new GraphCommandResultBuilder(violations).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommandResult<RuleViolation> undo(final GraphCommandExecutionContext context) {
        //Remove existing relationship
        getEdgeForTarget(in, child, sequencePredicate())
                .ifPresent((e) -> removeRelationship(e,
                                                     in.get(),
                                                     child,
                                                     context));
        getEdgeForSource(out, child, sequencePredicate())
                .ifPresent((e) -> removeRelationship(e,
                                                     child,
                                                     out.get(),
                                                     context));

        getEdgeForTarget(Optional.of(parent), child, childPredicate())
                .ifPresent((e) -> removeRelationship(e,
                                                     parent,
                                                     child,
                                                     context));
        edge.ifPresent(e -> addRelationship(in.get(),
                                            out.get(),
                                            OptionalInt.empty(),
                                            context,
                                            SequenceFlow::new));

        //Add new relationship
        if (originalIn.isPresent() && originalOut.isPresent()) {
            getEdgeForTarget(originalIn, originalOut.get(), sequencePredicate())
                    .ifPresent((e) -> removeRelationship(e,
                                                         originalIn.get(),
                                                         originalOut.get(),
                                                         context));
        }

        originalOut.ifPresent((p) -> addRelationship(child,
                                                     originalOut.get(),
                                                     OptionalInt.empty(),
                                                     context,
                                                     SequenceFlow::new));

        originalIn.ifPresent((p) -> addRelationship(originalIn.get(),
                                                    child,
                                                    OptionalInt.empty(),
                                                    context,
                                                    SequenceFlow::new));

        originalParent.ifPresent((p) -> addRelationship(originalParent.get(),
                                                        child,
                                                        originalIndex,
                                                        context,
                                                        Child::new));

        return GraphCommandResultBuilder.SUCCESS;
    }
}
