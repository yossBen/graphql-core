/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.graphql.provider.dxm;

import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.GraphQLAnnotationsComponent;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.GraphQLExtensionsHandler;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.servlet.GraphQLMutationProvider;
import graphql.servlet.GraphQLProvider;
import graphql.servlet.GraphQLQueryProvider;
import graphql.servlet.GraphQLTypesProvider;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNode;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNodeImpl;
import org.jahia.modules.graphql.provider.dxm.node.SpecializedType;
import org.jahia.modules.graphql.provider.dxm.node.SpecializedTypesHandler;
import org.jahia.modules.graphql.provider.dxm.relay.DXRelay;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Component(service = GraphQLProvider.class, immediate = true)
public class DXGraphQLProvider implements GraphQLTypesProvider, GraphQLQueryProvider, GraphQLMutationProvider, DXGraphQLExtensionsProvider {
    private static Logger logger = LoggerFactory.getLogger(GraphQLQueryProvider.class);

    private static DXGraphQLProvider instance;

    private SpecializedTypesHandler specializedTypesHandler;

    private GraphQLAnnotationsComponent graphQLAnnotations;

    private ProcessingElementsContainer container;

    private Collection<DXGraphQLExtensionsProvider> extensionsProviders = new HashSet<>();

    private GraphQLObjectType queryType;
    private GraphQLObjectType mutationType;

    public static DXGraphQLProvider getInstance() {
        return instance;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policyOption = ReferencePolicyOption.GREEDY)
    public void setGraphQLAnnotations(GraphQLAnnotationsComponent graphQLAnnotations) {
        this.graphQLAnnotations = graphQLAnnotations;
    }

    public GraphQLAnnotationsComponent getGraphQLAnnotations() {
        return graphQLAnnotations;
    }

    public ProcessingElementsContainer getContainer() {
        return container;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY)
    public void addExtensionProvider(DXGraphQLExtensionsProvider provider) {
        this.extensionsProviders.add(provider);
    }

    public void removeExtensionProvider(DXGraphQLExtensionsProvider provider) {
        this.extensionsProviders.remove(provider);
    }

    @Activate
    public void activate() {
        instance = this;

        container = graphQLAnnotations.createContainer();
        specializedTypesHandler = new SpecializedTypesHandler(graphQLAnnotations, container);

        GraphQLExtensionsHandler extensionsHandler = graphQLAnnotations.getExtensionsHandler();

        container.setRelay(new DXRelay());

        extensionsProviders.add(this);

        for (DXGraphQLExtensionsProvider extensionsProvider : extensionsProviders) {
            for (Class<?> aClass : extensionsProvider.getExtensions()) {
                extensionsHandler.registerTypeExtension(aClass, container);
            }
            for (Class<? extends GqlJcrNode> aClass : extensionsProvider.getSpecializedTypes()) {
                SpecializedType annotation = aClass.getAnnotation(SpecializedType.class);
                if (annotation != null) {
                    specializedTypesHandler.addType(annotation.value(), aClass);
                } else {
                    logger.error("No annotation found on class "+aClass);
                }
            }
        }

        queryType = (GraphQLObjectType) graphQLAnnotations.getOutputTypeProcessor().getOutputTypeOrRef(Query.class, container);
        mutationType = (GraphQLObjectType) graphQLAnnotations.getOutputTypeProcessor().getOutputTypeOrRef(Mutation.class, container);

        for (DXGraphQLExtensionsProvider extensionsProvider : extensionsProviders) {
            for (Class<?> aClass : extensionsProvider.getExtensions()) {
                extensionsHandler.registerTypeExtension(aClass, container);
            }
        }

        specializedTypesHandler.initializeTypes();

    }

    @Override
    public Collection<GraphQLType> getTypes() {
        List<GraphQLType> types = new ArrayList<>();

        types.add(graphQLAnnotations.getOutputTypeProcessor().getOutputTypeOrRef(GqlJcrNodeImpl.class, container));
        types.addAll(specializedTypesHandler.getKnownTypes().values());
        return types;
    }

    @Override
    public Collection<GraphQLFieldDefinition> getQueries() {
        return queryType.getFieldDefinitions();
    }

    @Override
    public Collection<GraphQLFieldDefinition> getMutations() {
        return mutationType.getFieldDefinitions();
    }


    public GraphQLOutputType getOutputType(Class<?> clazz) {
        return graphQLAnnotations.getOutputTypeProcessor().getOutputTypeOrRef(clazz, container);
    }

    @GraphQLName("Query")
    public static class Query {
    }

    @GraphQLName("Mutation")
    public static class Mutation {
    }

}
