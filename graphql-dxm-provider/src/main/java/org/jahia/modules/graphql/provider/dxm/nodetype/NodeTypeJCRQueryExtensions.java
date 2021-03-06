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

import graphql.ErrorType;
import graphql.annotations.annotationTypes.*;
import graphql.annotations.connection.GraphQLConnection;
import graphql.schema.DataFetchingEnvironment;
import org.jahia.modules.graphql.provider.dxm.BaseGqlClientException;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrQuery;
import org.jahia.modules.graphql.provider.dxm.relay.DXPaginatedData;
import org.jahia.modules.graphql.provider.dxm.relay.DXPaginatedDataConnectionFetcher;
import org.jahia.modules.graphql.provider.dxm.relay.PaginationHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeIterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@GraphQLTypeExtension(GqlJcrQuery.class)
public class NodeTypeJCRQueryExtensions {

    private GqlJcrQuery source;

    public NodeTypeJCRQueryExtensions(GqlJcrQuery source) {
        this.source = source;
    }

    @GraphQLField
    @GraphQLDescription("Get a nodetype by its name")
    public static GqlJcrNodeType getNodeTypeByName(@GraphQLNonNull @GraphQLName("name") String name) {
        try {
            return new GqlJcrNodeType(NodeTypeRegistry.getInstance().getNodeType(name));
        } catch (NoSuchNodeTypeException e) {
            throw new BaseGqlClientException(e, ErrorType.DataFetchingException);
        }
    }

    @GraphQLField
    @GraphQLDescription("Get a list of nodetypes based on specified parameter")
    @GraphQLConnection(connection = DXPaginatedDataConnectionFetcher.class)
    public static DXPaginatedData<GqlJcrNodeType> getNodeTypes(@GraphQLName("filter") NodeTypesListInput input,DataFetchingEnvironment environment) {
        PaginationHelper.Arguments arguments = PaginationHelper.parseArguments(environment);
        NodeTypeRegistry registry = NodeTypeRegistry.getInstance();
        NodeTypeIterator nodeTypes = (input == null || input.getModules() == null) ? registry.getAllNodeTypes() : registry.getAllNodeTypes(input.getModules());
        List<GqlJcrNodeType> mapped = Stream.generate(() -> ((ExtendedNodeType) nodeTypes.nextNodeType()))
                .limit(nodeTypes.getSize())
                .filter(n -> (n.isMixin() && (input == null || input.getIncludeMixins())) || (!n.isMixin() && (input == null || input.getIncludeNonMixins())))
                .map(GqlJcrNodeType::new)
                .collect(Collectors.toList());

        return PaginationHelper.paginate(mapped, GqlJcrNodeType::getName, arguments);
    }

}
