/*
 *  ==========================================================================================
 *  =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 *  ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 *      Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *      THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *      1/GPL OR 2/JSEL
 *
 *      1/ GPL
 *      ==================================================================================
 *
 *      IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *      2/ JSEL - Commercial and Supported Versions of the program
 *      ===================================================================================
 *
 *      IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *      Alternatively, commercial and supported versions of the program - also known as
 *      Enterprise Distributions - must be used in accordance with the terms and conditions
 *      contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *      If you are unsure which license is appropriate for your use,
 *      please contact the sales department at sales@jahia.com.
 *
 */

package org.jahia.modules.graphql.provider.dxm.node;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import org.jahia.modules.graphql.provider.dxm.relay.GqlNode;
import org.jahia.modules.graphql.provider.dxm.relay.NodeFetcher;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;
import java.nio.charset.StandardCharsets;

import static java.util.Base64.getDecoder;

public class JcrNodeFetcher implements NodeFetcher {

    private SpecializedTypesHandler.NodeTypeResolver nodeTypeResolver;

    public JcrNodeFetcher() {
        nodeTypeResolver = new SpecializedTypesHandler.NodeTypeResolver();
    }

    @Override
    public boolean canHandle(String id) {
        String decoded = new String(getDecoder().decode(id), StandardCharsets.UTF_8);
        return decoded.startsWith("jcrnode:");
    }

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment env) {
        return nodeTypeResolver.getType(env);
    }

    @Override
    public GqlNode getNode(String id) {
        try {
            String decoded = new String(getDecoder().decode(id), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(parts[1]);
            JCRNodeWrapper node = session.getNodeByIdentifier(parts[2]);
            return (GqlNode) SpecializedTypesHandler.getNode(node);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }


}