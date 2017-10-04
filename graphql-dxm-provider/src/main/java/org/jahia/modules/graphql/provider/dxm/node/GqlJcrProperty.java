package org.jahia.modules.graphql.provider.dxm.node;

import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;
import graphql.annotations.GraphQLNonNull;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRValueWrapper;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

/**
 * GraphQL representation of a JCR property.
 */
@GraphQLName("JCRProperty")
public class GqlJcrProperty {

    private JCRPropertyWrapper property;
    private GqlJcrNode parentNode;

    /**
     * Create an instance that represents a JCR property to GraphQL.
     *
     * @param property The JCR property to represent
     * @param parentNode The GraphQL representation of the JCR node the property belongs to
     */
    public GqlJcrProperty(JCRPropertyWrapper property, GqlJcrNode parentNode) {
        this.property = property;
        this.parentNode = parentNode;
    }

    /**
     * @return The name of the JCR property
     */
    @GraphQLField
    @GraphQLNonNull
    public String getName() {
        try {
            return property.getName();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The type of the JCR property
     */
    @GraphQLField
    @GraphQLNonNull
    public GqlJcrPropertyType getType() {
        try {
            return GqlJcrPropertyType.getValue(property.getType());
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The value of the JCR property as a String in case the property is single-valued, null otherwise
     */
    @GraphQLField
    public String getValue() {
        try {
            if (property.isMultiple()) {
                return null;
            }
            return property.getValue().getString();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The values of the JCR property as a Strings in case the property is multiple-valued, null otherwise
     */
    @GraphQLField
    public List<String> getValues() {
        try {
            if (!property.isMultiple()) {
                return null;
            }
            List<String> values = new ArrayList<>();
            for (JCRValueWrapper wrapper : property.getValues()) {
                values.add(wrapper.getString());
            }
            return values;
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The GraphQL representation of the JCR node the property belongs to.
     */
    @GraphQLField
    @GraphQLNonNull
    public GqlJcrNode getParentNode() {
        return parentNode;
    }
}
