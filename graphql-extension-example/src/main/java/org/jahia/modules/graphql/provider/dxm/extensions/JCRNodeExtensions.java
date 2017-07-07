package org.jahia.modules.graphql.provider.dxm.extensions;

import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;
import graphql.annotations.GraphQLTypeExtension;
import graphql.schema.DataFetchingEnvironment;
import org.jahia.modules.graphql.provider.dxm.node.DXGraphQLJCRNode;

@GraphQLTypeExtension(DXGraphQLJCRNode.class)
public class JCRNodeExtensions {

    @GraphQLField
    public static String testExtension(DataFetchingEnvironment env, @GraphQLName("arg") String arg) {
        DXGraphQLJCRNode n = env.getSource();
        return "test " + n.getName() + " - " + arg;
    }

}
