/*
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

import java.util.List;

@GraphQLName("JCRProperty")
@GraphQLDescription("GraphQL representation of a JCR property to set")
public class GqlJcrPropertyInput {

    public GqlJcrPropertyInput(@GraphQLName("name") @GraphQLNonNull String name,
                               @GraphQLName("language") String language,
                               @GraphQLName("type") GqlJcrPropertyType type,
                               @GraphQLName("value") String value,
                               @GraphQLName("values") List<String> values) {
        this.name = name;
        this.language = language;
        this.type = type;
        this.value = value;
        this.values = values;
    }

    @GraphQLField
    @GraphQLDescription("The name of the property to set")
    public String name;

    @GraphQLField
    @GraphQLDescription("The language in which the property will be set (for internationalized properties")
    public String language;

    @GraphQLField
    @GraphQLDescription("The type of the property")
    public GqlJcrPropertyType type;

    @GraphQLField
    @GraphQLDescription("The value to set (for non multiple properties)")
    public String value;

    @GraphQLField
    @GraphQLDescription("The values to set (for multiple properties)")
    public List<String> values;

}
