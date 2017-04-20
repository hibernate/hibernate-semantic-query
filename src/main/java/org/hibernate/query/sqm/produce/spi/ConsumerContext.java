/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.produce.spi;

import org.hibernate.query.sqm.StrictJpaComplianceViolation;
import org.hibernate.query.sqm.domain.SqmDomainMetamodel;

/**
 * Contextual information related to the consumer/caller of the parser - a callback API.
 *
 * @author Steve Ebersole
 *
 * @deprecated Use SessionFactoryImplementor instead
 */
@Deprecated
public interface ConsumerContext {
	// todo (6.0) - remove in preference for SessionFactoryImplementor

	/**
	 * Access to the metamodel describing the underlying domain model.
	 *
	 * @return The domain metamodel.
	 */
	SqmDomainMetamodel getDomainMetamodel();

	/**
	 * Resolve any non-classified class reference encountered in the query.
	 * Generally this is used to resolve constant expressions and
	 * dynamic-instantiation targets.
	 *
	 * @param name The name of the class to locate
	 *
	 * @return The Class reference, never {@code null}.
	 *
	 * @throws ClassNotFoundException If the Class could not be located by name
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
