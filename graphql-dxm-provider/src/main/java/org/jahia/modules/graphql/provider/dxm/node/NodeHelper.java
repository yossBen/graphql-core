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

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.functors.AllPredicate;
import org.apache.commons.collections4.functors.AnyPredicate;
import org.apache.commons.collections4.functors.NonePredicate;
import org.apache.commons.collections4.functors.TruePredicate;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

public class NodeHelper {

    private static HashMap<GqlJcrNode.PropertyEvaluation, PropertyEvaluationAlgorithm> ALGORITHM_BY_EVALUATION = new HashMap<>();

    static {

        ALGORITHM_BY_EVALUATION.put(GqlJcrNode.PropertyEvaluation.PRESENT, new PropertyEvaluationAlgorithm() {

            @Override
            public boolean evaluate(JCRNodeWrapper node, String language, String propertyName, String propertyValue) {
                return hasProperty(node, language, propertyName);
            }
        });

        ALGORITHM_BY_EVALUATION.put(GqlJcrNode.PropertyEvaluation.ABSENT, new PropertyEvaluationAlgorithm() {

            @Override
            public boolean evaluate(JCRNodeWrapper node, String language, String propertyName, String propertyValue) {
                return !hasProperty(node, language, propertyName);
            }
        });

        ALGORITHM_BY_EVALUATION.put(GqlJcrNode.PropertyEvaluation.EQUAL, new PropertyEvaluationAlgorithm() {

            @Override
            public boolean evaluate(JCRNodeWrapper node, String language, String propertyName, String propertyValue) {
                if (propertyValue == null) {
                    throw new GqlJcrWrongInputException("Property value is required for " + GqlJcrNode.PropertyEvaluation.EQUAL + " evaluation");
                }
                return hasPropertyValue(node, language, propertyName, propertyValue);
            }
        });

        ALGORITHM_BY_EVALUATION.put(GqlJcrNode.PropertyEvaluation.DIFFERENT, new PropertyEvaluationAlgorithm() {

            @Override
            public boolean evaluate(JCRNodeWrapper node, String language, String propertyName, String propertyValue) {
                if (propertyValue == null) {
                    throw new GqlJcrWrongInputException("Property value is required for " + GqlJcrNode.PropertyEvaluation.DIFFERENT + " evaluation");
                }
                return !hasPropertyValue(node, language, propertyName, propertyValue);
            }
        });
    }

    public static Predicate<JCRNodeWrapper> getPropertiesPredicate(GqlJcrNode.NodePropertiesInput propertiesFilter) {
        Predicate<JCRNodeWrapper> propertiesPredicate;
        if (propertiesFilter == null) {
            propertiesPredicate = TruePredicate.truePredicate();
        } else {
            LinkedList<Predicate<JCRNodeWrapper>> propertyPredicates = new LinkedList<>();
            for (GqlJcrNode.NodePropertyInput propertyFilter : propertiesFilter.getPropertyFilters()) {
                GqlJcrNode.PropertyEvaluation propertyEvaluation = propertyFilter.getPropertyEvaluation();
                if (propertyEvaluation == null) {
                    propertyEvaluation = GqlJcrNode.PropertyEvaluation.EQUAL;
                }
                PropertyEvaluationAlgorithm evaluationAlgorithm = ALGORITHM_BY_EVALUATION.get(propertyEvaluation);
                if (evaluationAlgorithm == null) {
                    throw new IllegalArgumentException("Unknown property evaluation: " + propertyEvaluation);
                }
                propertyPredicates.add(node -> evaluationAlgorithm.evaluate(node, propertyFilter.getLanguage(), propertyFilter.getPropertyName(), propertyFilter.getPropertyValue()));
            }
            propertiesPredicate = getCombinedPredicate(propertyPredicates, propertiesFilter.getMulticriteriaEvaluation(), GqlJcrNode.MulticriteriaEvaluation.ALL);
        }
        return propertiesPredicate;
    }

    public static Predicate<JCRNodeWrapper> getTypesPredicate(GqlJcrNode.NodeTypesInput typesFilter) {
        Predicate<JCRNodeWrapper> typesPredicate;
        if (typesFilter == null) {
            typesPredicate = TruePredicate.truePredicate();
        } else {
            LinkedList<Predicate<JCRNodeWrapper>> typePredicates = new LinkedList<>();
            for (String typeFilter : typesFilter.getTypes()) {
                typePredicates.add(node -> {
                    try {
                        return node.isNodeType(typeFilter);
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            typesPredicate = getCombinedPredicate(typePredicates, typesFilter.getMulticriteriaEvaluation(), GqlJcrNode.MulticriteriaEvaluation.ANY);
        }
        return typesPredicate;
    }

    private static <T> Predicate<T> getCombinedPredicate(Collection<Predicate<T>> predicates, GqlJcrNode.MulticriteriaEvaluation multicriteriaEvaluation, GqlJcrNode.MulticriteriaEvaluation defaultMulticriteriaEvaluation) {
        if (multicriteriaEvaluation == null) {
            multicriteriaEvaluation = defaultMulticriteriaEvaluation;
        }
        if (multicriteriaEvaluation == GqlJcrNode.MulticriteriaEvaluation.ALL) {
            return AllPredicate.allPredicate(predicates);
        } else if (multicriteriaEvaluation == GqlJcrNode.MulticriteriaEvaluation.ANY) {
            return AnyPredicate.anyPredicate(predicates);
        } else if (multicriteriaEvaluation == GqlJcrNode.MulticriteriaEvaluation.NONE) {
            return NonePredicate.nonePredicate(predicates);
        } else {
            throw new IllegalArgumentException("Unknown multicriteria evaluation: " + multicriteriaEvaluation);
        }
    }

    private static boolean hasProperty(JCRNodeWrapper node, String language, String propertyName) {
        try {
            node = getNodeInLanguage(node, language);
            return node.hasProperty(propertyName);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasPropertyValue(JCRNodeWrapper node, String language, String propertyName, String propertyValue) {
        try {
            node = getNodeInLanguage(node, language);
            if (!node.hasProperty(propertyName)) {
                return false;
            }
            return (node.getProperty(propertyName).getString().equals(propertyValue));
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }



    public static JCRNodeWrapper getNodeInLanguage(JCRNodeWrapper node, String language) throws RepositoryException {
        if (language == null) {
            return node;
        }
        String workspace = node.getSession().getWorkspace().getName();
        Locale locale = LanguageCodeConverters.languageCodeToLocale(language);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale);
        return session.getNodeByIdentifier(node.getIdentifier());
    }

    static void collectDescendants(JCRNodeWrapper node, Predicate<JCRNodeWrapper> predicate, boolean recurse, Collection<GqlJcrNode> descendants) throws RepositoryException {
        for (JCRNodeWrapper child : node.getNodes()) {
            if (predicate.evaluate(child)) {
                descendants.add(SpecializedTypesHandler.getNode(child));
            }
            if (recurse) {
                collectDescendants(child, predicate, true, descendants);
            }
        }
    }

    static Predicate<JCRNodeWrapper> getNodesPredicate(final Collection<String> names, final GqlJcrNode.NodeTypesInput typesFilter, final GqlJcrNode.NodePropertiesInput propertiesFilter) {

        Predicate<JCRNodeWrapper> namesPredicate;
        if (names == null) {
            namesPredicate = TruePredicate.truePredicate();
        } else {
            namesPredicate = new Predicate<JCRNodeWrapper>() {

                @Override
                public boolean evaluate(JCRNodeWrapper node) {
                    return names.contains(node.getName());
                }
            };
        }

        Predicate<JCRNodeWrapper> typesPredicate = getTypesPredicate(typesFilter);

        Predicate<JCRNodeWrapper> propertiesPredicate = getPropertiesPredicate(propertiesFilter);

        @SuppressWarnings("unchecked") Predicate<JCRNodeWrapper> result = AllPredicate.allPredicate(GqlJcrNodeImpl.DEFAULT_CHILDREN_PREDICATE, namesPredicate, typesPredicate, propertiesPredicate);
        return result;
    }

    private interface PropertyEvaluationAlgorithm {
        boolean evaluate(JCRNodeWrapper node, String language, String propertyName, String propertyValue);
    }



}
