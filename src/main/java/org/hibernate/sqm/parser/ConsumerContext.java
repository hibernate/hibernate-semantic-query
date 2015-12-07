/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser;

import org.hibernate.sqm.domain.DomainMetamodel;

/**
 * Contextual information related to the consumer/caller of the parser - a callback API.
 *
 * @author Steve Ebersole
 */
public interface ConsumerContext {
	/**
	 * Access to the metamodel describing the underlying domain model.
	 *
	 * @return The domain metamodel.
	 */
	DomainMetamodel getDomainMetamodel();

	/**
	 * Resolve any (potential) non-entity class reference encountered in the query.
	 *
	 * @param name The name of the class to locate
	 *
	 * @return The Class reference
	 *
	 * @throws ClassNotFoundException
	 */
	Class classByName(String name) throws ClassNotFoundException;

	/**
	 * Should constructs allowed by HQL but not allowed by JPQL result in an exception?
	 * <p/>
	 * When this returns {@code true}, detected violations lead to a
	 * {@link StrictJpaComplianceViolation} being thrown
	 *
	 * @return {@code true} indicates that parsing and validation should strictly adhere to
	 * the JPQL subset.  {@code false} indicates to allow the full HQL superset.
	 */
	boolean useStrictJpaCompliance();
}
