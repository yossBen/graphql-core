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

import graphql.ErrorType;
import graphql.annotations.annotationTypes.*;
import graphql.annotations.connection.GraphQLConnection;
import graphql.schema.DataFetchingEnvironment;
import org.jahia.modules.graphql.provider.dxm.BaseGqlClientException;
import org.jahia.modules.graphql.provider.dxm.relay.DXPaginatedData;
import org.jahia.modules.graphql.provider.dxm.relay.DXPaginatedDataConnectionFetcher;
import org.jahia.modules.graphql.provider.dxm.relay.PaginationHelper;
import org.jahia.services.content.*;
import org.jahia.services.query.QueryWrapper;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static org.jahia.modules.graphql.provider.dxm.node.GqlJcrQuery.QueryLanguage.SQL2;

/**
 * GraphQL root object for JCR related queries
 */
@GraphQLName("JCRQuery")
@GraphQLDescription("JCR Queries")
public class GqlJcrQuery {

    private String workspace;

    public GqlJcrQuery(String workspace) {
        this.workspace = workspace;
    }

    /**
     * JCR query languages available to use for nodes querying.
     */
    public enum QueryLanguage {

        /**
         * SQL2 query language.
         */
        SQL2(Query.JCR_SQL2),

        /**
         * XPath query language.
         */
        XPATH(Query.XPATH);

        private String jcrQueryLanguage;

        private QueryLanguage(String jcrQueryLanguage) {
            this.jcrQueryLanguage = jcrQueryLanguage;
        }

        /**
         * @return Corresponding language value defined by the JCR API
         */
        public String getJcrQueryLanguage() {
            return jcrQueryLanguage;
        }
    }

    /**
     * Get GraphQL representation of a node by its UUID.
     *
     * @param uuid The UUID of the node
     * @return GraphQL representation of the node
     * @throws BaseGqlClientException In case of issues fetching the node
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("Get GraphQL representation of a node by its UUID")
    public GqlJcrNode getNodeById(@GraphQLName("uuid") @GraphQLNonNull @GraphQLDescription("The UUID of the node") String uuid)
            throws BaseGqlClientException {
        try {
            return getGqlNodeById(uuid);
        } catch (RepositoryException e) {
            throw new BaseGqlClientException(e, ErrorType.DataFetchingException);
        }
    }

    /**
     * Get GraphQL representation of a node by its path.
     *
     * @param path The path of the node
     * @return GraphQL representation of the node
     * @throws BaseGqlClientException In case of issues fetching the node
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("Get GraphQL representation of a node by its path")
    public GqlJcrNode getNodeByPath(@GraphQLName("path") @GraphQLNonNull @GraphQLDescription("The path of the node") String path)
            throws BaseGqlClientException {
        try {
            return getGqlNodeByPath(path);
        } catch (RepositoryException e) {
            throw new BaseGqlClientException(e, ErrorType.DataFetchingException);
        }
    }

    /**
     * Get GraphQL representations of multiple nodes by their UUIDs.
     *
     * @param uuids The UUIDs of the nodes
     * @return GraphQL representations of the nodes
     * @throws BaseGqlClientException In case of issues fetching the nodes
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("Get GraphQL representations of multiple nodes by their UUIDs")
    public Collection<GqlJcrNode> getNodesById(@GraphQLName("uuids") @GraphQLNonNull @GraphQLDescription("The UUIDs of the nodes") Collection<@GraphQLNonNull String> uuids)
            throws BaseGqlClientException {
        try {
            List<GqlJcrNode> nodes = new ArrayList<>(uuids.size());
            for (String uuid : uuids) {
                nodes.add(getGqlNodeById(uuid));
            }
            return nodes;
        } catch (RepositoryException e) {
            throw new BaseGqlClientException(e, ErrorType.DataFetchingException);
        }
    }

    /**
     * Get GraphQL representations of multiple nodes by their paths.
     *
     * @param paths The paths of the nodes
     * @return GraphQL representations of the nodes
     * @throws BaseGqlClientException In case of issues fetching the nodes
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("Get GraphQL representations of multiple nodes by their paths")
    public Collection<GqlJcrNode> getNodesByPath(@GraphQLName("paths") @GraphQLNonNull @GraphQLDescription("The paths of the nodes") Collection<@GraphQLNonNull String> paths)
            throws BaseGqlClientException {
        try {
            List<GqlJcrNode> nodes = new ArrayList<>(paths.size());
            for (String path : paths) {
                nodes.add(getGqlNodeByPath(path));
            }
            return nodes;
        } catch (RepositoryException e) {
            throw new BaseGqlClientException(e, ErrorType.DataFetchingException);
        }
    }

    /**
     * Get GraphQL representations of nodes using a query language supported by JCR.
     *
     * @param query The query string
     * @param queryLanguage The query language
     * @return GraphQL representations of nodes selected according to the query supplied
     * @throws BaseGqlClientException In case of issues executing the query
     */
    @GraphQLField
    @GraphQLConnection(connection = DXPaginatedDataConnectionFetcher.class)
    @GraphQLDescription("Get GraphQL representations of nodes using a query language supported by JCR")
    public DXPaginatedData<GqlJcrNode> getNodesByQuery(@GraphQLName("query") @GraphQLNonNull @GraphQLDescription("The query string") String query,
                                                              @GraphQLName("queryLanguage") @GraphQLDefaultValue(QueryLanguageDefaultValue.class) @GraphQLDescription("The query language") QueryLanguage queryLanguage, DataFetchingEnvironment environment)
            throws BaseGqlClientException {
        try {
            PaginationHelper.Arguments arguments = PaginationHelper.parseArguments(environment);
            List<GqlJcrNode> result = new LinkedList<>();
            QueryManagerWrapper queryManager = getSession().getWorkspace().getQueryManager();
            QueryWrapper q = queryManager.createQuery(query, queryLanguage.getJcrQueryLanguage());
            JCRNodeIteratorWrapper nodes = q.execute().getNodes();
            while (nodes.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) nodes.next();
                result.add(SpecializedTypesHandler.getNode(node));
            }
            // todo: naive implementation of the pagination, could be improved in some cases by setting limit/offset in query
            return PaginationHelper.paginate(result, n -> PaginationHelper.encodeCursor(n.getUuid()), arguments);
        } catch (RepositoryException e) {
            throw new BaseGqlClientException(e, ErrorType.DataFetchingException);
        }
    }

    private GqlJcrNode getGqlNodeByPath(String path) throws RepositoryException {
        return SpecializedTypesHandler.getNode(getSession().getNode(path));
    }

    private GqlJcrNode getGqlNodeById(String uuid) throws RepositoryException {
        return SpecializedTypesHandler.getNode(getSession().getNodeByIdentifier(uuid));
    }

    private JCRSessionWrapper getSession() throws RepositoryException {
        return JCRSessionFactory.getInstance().getCurrentUserSession(workspace);
    }

    public static class QueryLanguageDefaultValue implements Supplier<Object> {

        @Override
        public GqlJcrQuery.QueryLanguage get() {
            return SQL2;
        }
    }

}
