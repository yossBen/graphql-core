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
package org.jahia.modules.graphql.provider.dxm.node;

import graphql.annotations.annotationTypes.*;
import graphql.annotations.connection.GraphQLConnection;
import graphql.schema.DataFetchingEnvironment;
import org.jahia.modules.graphql.provider.dxm.relay.DXPaginatedData;
import org.jahia.modules.graphql.provider.dxm.relay.DXPaginatedDataConnectionFetcher;
import org.jahia.services.content.JCRNodeWrapper;

import java.util.Collection;
import java.util.List;

/**
 * GraphQL representation of a JCR node.
 */
@GraphQLName("JCRNode")
@GraphQLTypeResolver(SpecializedTypesHandler.NodeTypeResolver.class)
@GraphQLDescription("GraphQL representation of a JCR node")
public interface GqlJcrNode {

    /**
     * @return The actual JCR node this object represents
     */
    JCRNodeWrapper getNode();

    /**
     * @return The type of the JCR node this object represents
     */
    String getType();

    /**
     * @return The UUID of the JCR node this object represents
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("The UUID of the JCR node this object represents")
    String getUuid();

    /**
     * @return The name of the JCR node this object represents
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("The name of the JCR node this object represents")
    String getName();

    /**
     * @return The path of the JCR node this object represents
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("The path of the JCR node this object represents")
    String getPath();

    /**
     * @param language The language to obtain the display name in
     * @return The display name of the JCR node this object represents in the requested language
     */
    @GraphQLField
    @GraphQLDescription("The display name of the JCR node this object represents in the requested language")
    String getDisplayName(@GraphQLName("language") @GraphQLDescription("The language to obtain the display name in") String language);

    /**
     * @return GraphQL representation of the parent JCR node
     */
    @GraphQLField
    @GraphQLDescription("GraphQL representation of the parent JCR node")
    GqlJcrNode getParent();

    /**
     * Get GraphQL representations of multiple of properties of the JCR node.
     *
     * @param names The names of the JCR properties; null to obtain all properties
     * @param language The language to obtain the properties in; must be a valid language code in case any internationalized properties are requested, does not matter for non-internationalized ones
     * @return GraphQL representations of the properties in the requested language
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("GraphQL representations of the properties in the requested language")
    Collection<GqlJcrProperty> getProperties(@GraphQLName("names") @GraphQLDescription("The names of the JCR properties; null to obtain all properties") Collection<String> names,
                                             @GraphQLName("language") @GraphQLDescription("The language to obtain the properties in; must be a valid language code in case any internationalized properties are requested, does not matter for non-internationalized ones") String language);

    /**
     * Get a GraphQL representation of a single property of the JCR node.
     *
     * @param name The name of the JCR property
     * @param language The language to obtain the property in; must be a valid language code for internationalized properties, does not matter for non-internationalized ones
     * @return The GraphQL representation of the property in the requested language; null if the property does not exist
     */
    @GraphQLField
    @GraphQLDescription("The GraphQL representation of the property in the requested language; null if the property does not exist")
    GqlJcrProperty getProperty(@GraphQLName("name") @GraphQLDescription("The name of the JCR property") @GraphQLNonNull String name,
                               @GraphQLName("language") @GraphQLDescription("The language to obtain the property in; must be a valid language code for internationalized properties, does not matter for non-internationalized ones") String language);

    /**
     * Get GraphQL representations of child nodes of the JCR node, according to filters specified. A child node must pass through all non-null filters in order to be included in the result.
     *
     * @param names Filter of child nodes by their names; null to avoid such filtering
     * @param typesFilter Filter of child nodes by their types; null to avoid such filtering
     * @param propertiesFilter Filter of child nodes by their property values; null to avoid such filtering
     * @return GraphQL representations of the child nodes, according to parameters passed
     * @throws GqlJcrWrongInputException In case any of the property filters passed as a part of the propertiesFilter is inconsistent (for example missing a property value to be used for comparison)
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLConnection(connection = DXPaginatedDataConnectionFetcher.class)
    @GraphQLDescription("GraphQL representations of the child nodes, according to parameters passed")
    DXPaginatedData<GqlJcrNode> getChildren(@GraphQLName("names") @GraphQLDescription("Filter of child nodes by their names; null to avoid such filtering") Collection<String> names,
                                            @GraphQLName("typesFilter") @GraphQLDescription("Filter of child nodes by their types; null to avoid such filtering") NodeTypesInput typesFilter,
                                            @GraphQLName("propertiesFilter") @GraphQLDescription("Filter of child nodes by their property values; null to avoid such filtering") NodePropertiesInput propertiesFilter,
                                            DataFetchingEnvironment environment)
    throws GqlJcrWrongInputException;

    /**
     * Get GraphQL representations of a child node, based on relative path.
     *
     * @param path Name or relative path of the sub node
     * @return GraphQL representations of the child node, according to parameters passed
     */
    @GraphQLField
    @GraphQLDescription("GraphQL representations of a child node, based on relative path")
    GqlJcrNode getChild(@GraphQLName("path") @GraphQLDescription("Name or relative path of the sub node") String path);

    /**
     * Get GraphQL representations of descendant nodes of the JCR node, according to filters specified. A descendant node must pass through all non-null filters in order to be included in the result.
     *
     * @param typesFilter Filter of descendant nodes by their types; null to avoid such filtering
     * @param propertiesFilter Filter of descendant nodes by their property values; null to avoid such filtering
     * @return GraphQL representations of the descendant nodes, according to parameters passed
     * @throws GqlJcrWrongInputException In case any of the property filters passed as a part of the propertiesFilter is inconsistent (for example missing a property value to be used for comparison)
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLConnection(connection = DXPaginatedDataConnectionFetcher.class)
    @GraphQLDescription("GraphQL representations of the descendant nodes, according to parameters passed")
    DXPaginatedData<GqlJcrNode> getDescendants(@GraphQLName("typesFilter") @GraphQLDescription("Filter of descendant nodes by their types; null to avoid such filtering") NodeTypesInput typesFilter,
                                    @GraphQLName("propertiesFilter") @GraphQLDescription("Filter of descendant nodes by their property values; null to avoid such filtering") NodePropertiesInput propertiesFilter,
                                               DataFetchingEnvironment environment)
    throws GqlJcrWrongInputException;

    /**
     * Get GraphQL representations of the ancestor nodes of the JCR node.
     *
     * @param upToPath The path of the topmost ancestor node to include in the result; null or empty string to include all the ancestor nodes
     * @return GraphQL representations of the ancestor nodes of the JCR node, top down direction
     * @throws GqlJcrWrongInputException In case the upToPath parameter value is not a valid path of an ancestor node of this node
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("GraphQL representations of the ancestor nodes of the JCR node, top down direction")
    List<GqlJcrNode> getAncestors(@GraphQLName("upToPath") @GraphQLDescription("The path of the topmost ancestor node to include in the result; null or empty string to include all the ancestor nodes") String upToPath)
    throws GqlJcrWrongInputException;

    /**
     * @return GraphQL representations of the reference properties that target the JCR Node
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLConnection(connection = DXPaginatedDataConnectionFetcher.class)
    @GraphQLDescription("GraphQL representations of the reference properties that target the current JCR Node")
    DXPaginatedData<GqlJcrProperty> getReferences(DataFetchingEnvironment environment);

    /**
     * Get GraphQL representation of this node in certain workspace.
     *
     * @param workspace The target workspace
     * @return GraphQL representation of this node in certain workspace; null in case there is no corresponding node in the target workspace
     */
    @GraphQLField
    @GraphQLDescription("GraphQL representation of this node in certain workspace")
    GqlJcrNode getNodeInWorkspace(@GraphQLName("workspace") @GraphQLDescription("The target workspace") @GraphQLNonNull NodeQueryExtensions.Workspace workspace);

    /**
     * A way to evaluate a criteria consisting of multiple sub-criteria.
     */
    enum MulticriteriaEvaluation {

        /**
         * The result criteria evaluates positive iff all sub-criteria evaluate positive.
         */
        @GraphQLDescription("The result criteria evaluates positive iff all sub-criteria evaluate positive")
        ALL,

        /**
         * The result criteria evaluates positive if any sub-criteria evaluates positive.
         */
        @GraphQLDescription("The result criteria evaluates positive if any sub-criteria evaluates positive")
        ANY,

        /**
         * The result criteria evaluates positive if no sub-criteria evaluates positive.
         */
        @GraphQLDescription("The result criteria evaluates positive if no sub-criteria evaluates positive")
        NONE
    }

    /**
     * Nodes filter based on their types.
     */
    static class NodeTypesInput {

        private MulticriteriaEvaluation multicriteriaEvaluation;
        private Collection<String> types;

        /**
         * Create a filter instance.
         *
         * @param multicriteriaEvaluation The way to combine multiple type criteria; null to use ANY by default
         * @param types Node type names required for a node to pass the filter
         */
        public NodeTypesInput(@GraphQLName("multi") MulticriteriaEvaluation multicriteriaEvaluation,
                              @GraphQLName("types") @GraphQLNonNull Collection<String> types) {
            this.multicriteriaEvaluation = multicriteriaEvaluation;
            this.types = types;
        }

        /**
         * @return The way to combine multiple type criteria; null indicates default (ANY)
         */
        @GraphQLField
        @GraphQLName("multi")
        @GraphQLDescription("The way to combine multiple type criteria; null indicates default (ANY)")
        public MulticriteriaEvaluation getMulticriteriaEvaluation() {
            return multicriteriaEvaluation;
        }

        /**
         * @return Node type names required for a node to pass the filter
         */
        @GraphQLField
        @GraphQLNonNull
        @GraphQLDescription("Node type names required for a node to pass the filter")
        public Collection<String> getTypes() {
            return types;
        }
    }

    /**
     * The way to evaluate a node property.
     */
    enum PropertyEvaluation {

        /**
         * The property is present.
         */
        @GraphQLDescription("The property is present")
        PRESENT,

        /**
         * The property is absent.
         */
        @GraphQLDescription("The property is absent")
        ABSENT,

        /**
         * The property value is equal to given one.
         */
        @GraphQLDescription("The property value is equal to given one")
        EQUAL,

        /**
         * The property value is different from given one.
         */
        @GraphQLDescription("The property value is different from given one")
        DIFFERENT
    }

    /**
     * Nodes filter based on their properties.
     */
    static class NodePropertiesInput {

        private MulticriteriaEvaluation multicriteriaEvaluation;
        private Collection<NodePropertyInput> propertyFilters;

        /**
         * Create a filter instance.
         *
         * @param multicriteriaEvaluation The way to combine multiple individual property filters; null to use ALL by default
         * @param propertyFilters Individual property filters
         */
        public NodePropertiesInput(@GraphQLName("multi") MulticriteriaEvaluation multicriteriaEvaluation,
                                   @GraphQLName("filters") @GraphQLNonNull Collection<NodePropertyInput> propertyFilters) {
            this.multicriteriaEvaluation = multicriteriaEvaluation;
            this.propertyFilters = propertyFilters;
        }

        /**
         * @return The way to combine multiple individual property filters; null indicates default (ALL)
         */
        @GraphQLField
        @GraphQLName("multi")
        @GraphQLDescription("The way to combine multiple individual property filters; null indicates default (ALL)")
        public MulticriteriaEvaluation getMulticriteriaEvaluation() {
            return multicriteriaEvaluation;
        }

        /**
         * @return Individual property filters
         */
        @GraphQLField
        @GraphQLName("filters")
        @GraphQLNonNull
        @GraphQLDescription("Individual property filters")
        public Collection<NodePropertyInput> getPropertyFilters() {
            return propertyFilters;
        }
    }

    /**
     * Nodes filter based on a single property.
     */
    static class NodePropertyInput {

        private String language;
        private PropertyEvaluation propertyEvaluation;
        private String propertyName;
        private String propertyValue;

        /**
         * Create a filter instance.
         *
         * @param language Language to use when evaluating the property; must be a valid language code for internationalized properties, does not matter for non-internationalized ones
         * @param propertyEvaluation The way to evaluate the property; null to use EQUAL by default
         * @param propertyName The name of the property to filter by
         * @param propertyValue The value to evaluate the property against; only required for EQUAL and DIFFERENT evaluation, does not matter for PRESENT and ABSENT
         */
        public NodePropertyInput(@GraphQLName("language") String language,
                                 @GraphQLName("evaluation") PropertyEvaluation propertyEvaluation,
                                 @GraphQLName("property") @GraphQLNonNull String propertyName,
                                 @GraphQLName("value") String propertyValue) {
            this.language = language;
            this.propertyEvaluation = propertyEvaluation;
            this.propertyName = propertyName;
            this.propertyValue = propertyValue;
        }

        /**
         * @return Language to use when evaluating the property
         */
        @GraphQLField
        @GraphQLDescription("Language to use when evaluating the property")
        public String getLanguage() {
            return language;
        }

        /**
         * @return The way to evaluate the property; null indicates default (EQUAL)
         */
        @GraphQLField
        @GraphQLName("evaluation")
        @GraphQLDescription("The way to evaluate the property; null indicates default (EQUAL)")
        public PropertyEvaluation getPropertyEvaluation() {
            return propertyEvaluation;
        }

        /**
         * @return The name of the property to filter by
         */
        @GraphQLField
        @GraphQLName("property")
        @GraphQLNonNull
        @GraphQLDescription("The name of the property to filter by")
        public String getPropertyName() {
            return propertyName;
        }

        /**
         * @return The value to evaluate the property against
         */
        @GraphQLField
        @GraphQLName("value")
        @GraphQLDescription("The value to evaluate the property against")
        public String getPropertyValue() {
            return propertyValue;
        }
    }
}
