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
package org.jahia.modules.graphql.provider.dxm.nodetype;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.annotations.connection.GraphQLConnection;
import graphql.annotations.connection.PaginatedData;
import graphql.schema.DataFetchingEnvironment;
import org.jahia.modules.graphql.provider.dxm.relay.DXPaginatedDataConnectionFetcher;
import org.jahia.modules.graphql.provider.dxm.relay.PaginationHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GraphQL representation of a JCR node type
 */
@GraphQLName("JCRNodeType")
@GraphQLDescription("GraphQL representation of a JCR node type")
public class GqlJcrNodeType {
    public static final Logger logger = LoggerFactory.getLogger(GqlJcrNodeType.class);

    private ExtendedNodeType nodeType;


    public GqlJcrNodeType(ExtendedNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public ExtendedNodeType getNodeType() {
        return nodeType;
    }

    @GraphQLField()
    public String getName() {
        return nodeType.getName();
    }

    @GraphQLField()
    public String getDisplayName(@GraphQLName("language") @GraphQLNonNull String language) {
        return nodeType.getLabel(LanguageCodeConverters.languageCodeToLocale(language));

    }

    @GraphQLField
    @GraphQLDescription("System ID of the node type, corresponding to the name of the module declaring it.")
    public String getSystemId() {
        return nodeType.getSystemId();
    }

    @GraphQLField
    @GraphQLDescription("Returns true if this is a mixin type; returns false otherwise.")
    public boolean isMixin() {
        return nodeType.isMixin();
    }

    @GraphQLField
    @GraphQLDescription("Returns true if this is an abstract node type; returns false otherwise.")
    public boolean isAbstract() {
        return nodeType.isAbstract();
    }

    @GraphQLField
    @GraphQLDescription("Returns true if nodes of this type must support orderable child nodes; returns false otherwise.")
    public boolean isHasOrderableChildNodes() {
        return nodeType.hasOrderableChildNodes();
    }

    @GraphQLField
    @GraphQLDescription("Returns true if the node type is queryable.")
    public boolean isQueryable() {
        return nodeType.isQueryable();
    }

    @GraphQLField
    @GraphQLDescription("Returns the name of the primary item (one of the child items of the nodes of this node type). If this node has no primary item, then this method null.")
    public GqlJcrItemDefinition getPrimaryItem() {
        String primaryItemName = nodeType.getPrimaryItemName();
        if (primaryItemName != null) {
            if (nodeType.getChildNodeDefinitionsAsMap().containsKey(primaryItemName)) {
                return new GqlJcrNodeDefinition(nodeType.getChildNodeDefinitionsAsMap().get(primaryItemName));
            }
            if (nodeType.getPropertyDefinitionsAsMap().containsKey(primaryItemName)) {
                return new GqlJcrPropertyDefinition(nodeType.getPropertyDefinitionsAsMap().get(primaryItemName));
            }
        }
        return null;
    }

    @GraphQLField
    @GraphQLDescription("Returns an array containing the property definitions of this node type.")
    public List<GqlJcrPropertyDefinition> getProperties() {
        return Arrays.stream(nodeType.getPropertyDefinitions()).map(GqlJcrPropertyDefinition::new).collect(Collectors.toList());
    }

    @GraphQLField
    @GraphQLDescription("Returns an array containing the child node definitions of this node type.")
    public List<GqlJcrNodeDefinition> getNodes() {
        return Arrays.stream(nodeType.getChildNodeDefinitions()).map(GqlJcrNodeDefinition::new).collect(Collectors.toList());
    }

    @GraphQLField
    @GraphQLConnection(connection = DXPaginatedDataConnectionFetcher.class)
    @GraphQLDescription("Returns all subtypes of this node type in the node type inheritance hierarchy.")
    public PaginatedData<GqlJcrNodeType> getSubtypes(DataFetchingEnvironment environment) {
        PaginationHelper.Arguments arguments = PaginationHelper.parseArguments(environment);
        List<GqlJcrNodeType> subTypes = nodeType.getSubtypesAsList().stream().map(GqlJcrNodeType::new).collect(Collectors.toList());
        return PaginationHelper.paginate(subTypes, t -> PaginationHelper.encodeCursor(t.getName()), arguments);
    }

    @GraphQLField
    @GraphQLDescription("Returns all supertypes of this node type in the node type inheritance hierarchy.")
    public List<GqlJcrNodeType> getSupertypes() {
        return nodeType.getSupertypeSet().stream().map(GqlJcrNodeType::new).collect(Collectors.toList());
    }
}
