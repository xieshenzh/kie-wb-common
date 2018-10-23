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
package org.kie.workbench.common.stunner.bpmn.backend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import bpsim.impl.BpsimFactoryImpl;
import bpsim.impl.BpsimPackageImpl;
import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.impl.DroolsFactoryImpl;
import org.jboss.drools.impl.DroolsPackageImpl;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.DefinitionsConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.PropertyWriterFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.BaseConverterFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.BpmnNode;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.GraphBuilder;
import org.kie.workbench.common.stunner.bpmn.backend.legacy.resource.JBPMBpmn2ResourceFactoryImpl;
import org.kie.workbench.common.stunner.bpmn.backend.legacy.resource.JBPMBpmn2ResourceImpl;
import org.kie.workbench.common.stunner.bpmn.backend.workitem.service.WorkItemDefinitionBackendService;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.backend.service.XMLEncoderDiagramMetadataMarshaller;
import org.kie.workbench.common.stunner.core.definition.service.DiagramMarshaller;
import org.kie.workbench.common.stunner.core.definition.service.DiagramMetadataMarshaller;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandManager;
import org.kie.workbench.common.stunner.core.graph.command.impl.GraphCommandFactory;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.core.rule.RuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Direct as in "skipping json encoding"
 *
 */
public abstract class BaseDirectDiagramMarshaller implements DiagramMarshaller<Graph, Metadata, Diagram<Graph, Metadata>> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseDirectDiagramMarshaller.class);

    private final XMLEncoderDiagramMetadataMarshaller diagramMetadataMarshaller;
    private final DefinitionManager definitionManager;
    private final RuleManager ruleManager;
    private final WorkItemDefinitionBackendService workItemDefinitionService;
    private final TypedFactoryManager typedFactoryManager;
    private final GraphCommandFactory commandFactory;
    private final GraphCommandManager commandManager;

    public BaseDirectDiagramMarshaller(
            final XMLEncoderDiagramMetadataMarshaller diagramMetadataMarshaller,
            final DefinitionManager definitionManager,
            final RuleManager ruleManager,
            final WorkItemDefinitionBackendService workItemDefinitionService,
            final FactoryManager factoryManager,
            final GraphCommandFactory commandFactory,
            final GraphCommandManager commandManager) {
        this.diagramMetadataMarshaller = diagramMetadataMarshaller;
        this.definitionManager = definitionManager;
        this.ruleManager = ruleManager;
        this.workItemDefinitionService = workItemDefinitionService;
        this.typedFactoryManager = new TypedFactoryManager(factoryManager);
        this.commandFactory = commandFactory;
        this.commandManager = commandManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String marshall(final Diagram<Graph, Metadata> diagram) throws IOException {
        LOG.debug("Starting diagram marshalling...");

        Bpmn2Resource resource = createBpmn2Resource();

        // we start converting from the root, then pull out the result
        PropertyWriterFactory propertyWriterFactory = new PropertyWriterFactory();
        DefinitionsConverter definitionsConverter =
                new DefinitionsConverter(createFromStunnerConverterFactory(diagram.getGraph(),
                                                                           propertyWriterFactory),
                                         propertyWriterFactory);

        Definitions definitions =
                definitionsConverter.toDefinitions();

        resource.getContents().add(definitions);

        LOG.debug("Diagram marshalling completed successfully.");
        String outputString = renderToString(resource);
        LOG.trace(outputString);
        return outputString;
    }

    private String renderToString(Bpmn2Resource resource) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resource.save(outputStream, new HashMap<>());
        return StringEscapeUtils.unescapeHtml4(
                outputStream.toString("UTF-8"));
    }

    @Override
    public Graph<DefinitionSet, Node> unmarshall(final Metadata metadata,
                                                 final InputStream inputStream) throws IOException {
        LOG.debug("Starting diagram unmarshalling...");

        // definition resolver provides utlities to access elements of the BPMN datamodel
        DefinitionResolver definitionResolver =
                new DefinitionResolver(
                        parseDefinitions(inputStream),
                        workItemDefinitionService.execute(metadata));

        metadata.setCanvasRootUUID(definitionResolver.getDefinitions().getId());
        metadata.setTitle(definitionResolver.getProcess().getName());

        BaseConverterFactory converterFactory =
                createToStunnerConverterFactory(definitionResolver, typedFactoryManager);

        // perform actual conversion. Process is the root of the diagram
        BpmnNode diagramRoot =
                converterFactory
                        .rootProcessConverter()
                        .convertProcess();

        LOG.debug("Diagram unmarshalling completed successfully.");

        // the root node contains all of the information
        // needed to build the entire graph (including parent/child relationships)
        // thus, we can now walk the graph to issue all the commands
        // to draw it on our canvas
        Diagram<Graph<DefinitionSet, Node>, Metadata> diagram =
                typedFactoryManager.newDiagram(
                        definitionResolver.getDefinitions().getId(),
                        getDefinitionSetClass(),
                        metadata);
        GraphBuilder graphBuilder =
                new GraphBuilder(
                        diagram.getGraph(),
                        definitionManager,
                        typedFactoryManager,
                        ruleManager,
                        commandFactory,
                        commandManager);
        graphBuilder.render(diagramRoot);

        LOG.debug("Diagram drawing completed successfully.");
        return diagram.getGraph();
    }

    private Bpmn2Resource createBpmn2Resource() {
        DroolsFactoryImpl.init();
        BpsimFactoryImpl.init();

        ResourceSet rSet = new ResourceSetImpl();

        rSet.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put("bpmn2",
                     new JBPMBpmn2ResourceFactoryImpl());

        Bpmn2Resource resource =
                (Bpmn2Resource) rSet.createResource(
                        URI.createURI("virtual.bpmn2"));

        rSet.getResources().add(resource);
        return resource;
    }

    @Override
    public DiagramMetadataMarshaller<Metadata> getMetadataMarshaller() {
        return diagramMetadataMarshaller;
    }

    private static Definitions parseDefinitions(final InputStream inputStream) throws IOException {
        DroolsPackageImpl.init();
        BpsimPackageImpl.init();

        final ResourceSet resourceSet = new ResourceSetImpl();
        Resource.Factory.Registry resourceFactoryRegistry = resourceSet.getResourceFactoryRegistry();
        resourceFactoryRegistry.getExtensionToFactoryMap().put(
                Resource.Factory.Registry.DEFAULT_EXTENSION, new JBPMBpmn2ResourceFactoryImpl());

        EPackage.Registry packageRegistry = resourceSet.getPackageRegistry();
        packageRegistry.put("http://www.omg.org/spec/BPMN/20100524/MODEL", Bpmn2Package.eINSTANCE);
        packageRegistry.put("http://www.jboss.org/drools", DroolsPackage.eINSTANCE);

        final JBPMBpmn2ResourceImpl resource =
                (JBPMBpmn2ResourceImpl) resourceSet
                        .createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));

        resource.getDefaultLoadOptions()
                .put(JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
        resource.setEncoding("UTF-8");

        final Map<String, Object> options = new HashMap<>();
        options.put(JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
        options.put(JBPMBpmn2ResourceImpl.OPTION_DEFER_IDREF_RESOLUTION, true);
        options.put(JBPMBpmn2ResourceImpl.OPTION_DISABLE_NOTIFY, true);
        options.put(JBPMBpmn2ResourceImpl.OPTION_PROCESS_DANGLING_HREF,
                    JBPMBpmn2ResourceImpl.OPTION_PROCESS_DANGLING_HREF_RECORD);

        resource.load(inputStream, options);

        final DocumentRoot root = (DocumentRoot) resource.getContents().get(0);

        return root.getDefinitions();
    }

    protected abstract org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.BaseConverterFactory createFromStunnerConverterFactory(
            final Graph graph,
            final PropertyWriterFactory propertyWriterFactory);

    protected abstract org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.BaseConverterFactory createToStunnerConverterFactory(
            final DefinitionResolver definitionResolver,
            final TypedFactoryManager typedFactoryManager);

    protected abstract Class<?> getDefinitionSetClass();
}
