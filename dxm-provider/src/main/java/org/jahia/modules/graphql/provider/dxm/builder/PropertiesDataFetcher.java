package org.jahia.modules.graphql.provider.dxm.builder;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.jahia.modules.graphql.provider.dxm.model.DXGraphQLNode;
import org.jahia.modules.graphql.provider.dxm.model.DXGraphQLProperty;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

class PropertiesDataFetcher implements DataFetcher {
    @Override
    public Object get(DataFetchingEnvironment dataFetchingEnvironment) {
        DXGraphQLNode node = (DXGraphQLNode) dataFetchingEnvironment.getSource();
        List<DXGraphQLProperty> propertyList = new ArrayList<DXGraphQLProperty>();
        try {
            List<String> names = dataFetchingEnvironment.getArgument("names");
            String language = dataFetchingEnvironment.getArgument("language");
            JCRNodeWrapper jcrNode = node.getNode();
            if (language != null) {
                jcrNode = JCRSessionFactory.getInstance().getCurrentUserSession(null, LanguageCodeConverters.languageCodeToLocale(language))
                        .getNodeByIdentifier(jcrNode.getIdentifier());
            }
            if (names != null && !names.isEmpty()) {
                for (String name : names) {
                    if (jcrNode.hasProperty(name)) {
                        propertyList.add(new DXGraphQLProperty(jcrNode.getProperty(name)));
                    }
                }
            } else {
                PropertyIterator pi = jcrNode.getProperties();
                while (pi.hasNext()) {
                    JCRPropertyWrapper property = (JCRPropertyWrapper) pi.nextProperty();
                    propertyList.add(new DXGraphQLProperty(property));
                }
            }
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return propertyList;
    }
}
