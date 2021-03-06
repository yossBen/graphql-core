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

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import java.util.ArrayList;
import java.util.List;

/**
 * GraphQL representation of a JCR property.
 */
@GraphQLName("JCRProperty")
@GraphQLDescription("GraphQL representation of a JCR property.")
public class GqlJcrProperty {

    private JCRPropertyWrapper property;
    private GqlJcrNode node;

    /**
     * Create an instance that represents a JCR property to GraphQL.
     *
     * @param property The JCR property to represent
     * @param node The GraphQL representation of the JCR node the property belongs to
     */
    public GqlJcrProperty(JCRPropertyWrapper property, GqlJcrNode node) {
        this.property = property;
        this.node = node;
    }

    /**
     * Get underlying JCR property
     * @return
     */
    public JCRPropertyWrapper getProperty() {
        return property;
    }

    /**
     * @return The name of the JCR property
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("The name of the JCR property")
    public String getName() {
        try {
            return property.getName();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The path of the JCR property
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("The path of the JCR property")
    public String getPath() {
        try {
            return property.getPath();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The type of the JCR property
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("The type of the JCR property")
    public GqlJcrPropertyType getType() {
        try {
            return GqlJcrPropertyType.fromValue(property.getType());
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Whether the property is internationalized
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("Whether the property is internationalized")
    public boolean isInternationalized() {
        ExtendedPropertyDefinition propertyDefinition;
        try {
            propertyDefinition = node.getNode().getApplicablePropertyDefinition(getName(), property.getType(), property.isMultiple());
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return propertyDefinition.isInternationalized();
    }

    /**
     * @return The language the property value was obtained in for internationalized properties; null for non-internationalized ones
     */
    @GraphQLField
    @GraphQLDescription("The language the property value was obtained in for internationalized properties; null for non-internationalized ones")
    public String getLanguage() {
        try {
            return property.getLocale();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The value of the JCR property as a String in case the property is single-valued, null otherwise
     */
    @GraphQLField
    @GraphQLDescription("The value of the JCR property as a String in case the property is single-valued, null otherwise")
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
    @GraphQLDescription("The values of the JCR property as a Strings in case the property is multiple-valued, null otherwise")
    public List<String> getValues() {
        try {
            if (!property.isMultiple()) {
                return null;
            }
            JCRValueWrapper[] values = property.getValues();
            List<String> result = new ArrayList<>(values.length);
            for (JCRValueWrapper value : values) {
                result.add(value.getString());
            }
            return result;
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return GraphQL representation of the node this property references in case the property is single-valued, null otherwise
     * @throws GqlJcrUnresolvedNodeReferenceException In case either the type (must be REFEENCE, WEAKREFERENCE or STRING) or the actual value of the property do not allow to resolve the node reference
     */
    @GraphQLField
    @GraphQLDescription("GraphQL representation of the node this property references in case the property is single-valued, null otherwise")
    public GqlJcrNode getRefNode() throws GqlJcrUnresolvedNodeReferenceException {
        try {
            if (property.isMultiple()) {
                return null;
            }
            return getRefNode(property.getValue());
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return GraphQL representations of the nodes this property references in case the property is multiple-valued, null otherwise
     * @throws GqlJcrUnresolvedNodeReferenceException In case either the type (must be REFEENCE, WEAKREFERENCE or STRING) or any of the actual values of the property do not allow to resolve the node reference
     */
    @GraphQLField
    @GraphQLDescription("GraphQL representations of the nodes this property references in case the property is multiple-valued, null otherwise")
    public List<GqlJcrNode> getRefNodes() throws GqlJcrUnresolvedNodeReferenceException {
        try {
            if (!property.isMultiple()) {
                return null;
            }
            JCRValueWrapper[] values = property.getValues();
            List<GqlJcrNode> result = new ArrayList<>(values.length);
            for (JCRValueWrapper value : values) {
                result.add(getRefNode(value));
            }
            return result;
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The GraphQL representation of the JCR node the property belongs to.
     */
    @GraphQLField
    @GraphQLNonNull
    @GraphQLDescription("The GraphQL representation of the JCR node the property belongs to.")
    public GqlJcrNode getNode() {
        return node;
    }

    private GqlJcrNode getRefNode(JCRValueWrapper value) throws RepositoryException {
        JCRNodeWrapper refNode;
        try {
            refNode = value.getNode();
        } catch (ValueFormatException e) {
            throw new GqlJcrUnresolvedNodeReferenceException("The '" + property.getName() + "' property is not of a reference type", e);
        }
        if (refNode == null) {
            throw new GqlJcrUnresolvedNodeReferenceException("The value of the '" + property.getName() + "' property does not reference an existing node");
        }
        return SpecializedTypesHandler.getNode(refNode);
    }
}
