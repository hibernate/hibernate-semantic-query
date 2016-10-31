/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;

/**
 * Context for PathResolver implementations to locate FromElements
 *
 * @author Steve Ebersole
 */
public interface FromElementLocator {
	/**
	 * Find a FromElement by its identification variable (JPA term for alias).  Will search any parent contexts
	 *
	 * @param identificationVariable The identification variable
	 *
	 * @return matching FromElement, or {@code null}
	 */
	DomainReferenceBinding findFromElementByIdentificationVariable(String identificationVariable);

	/**
	 * Find a FromElement which exposes the given attribute.  Will search any parent contexts
	 *
	 * @param attributeName The name of the attribute to find a FromElement for
	 *
	 * @return matching FromElement, or {@code null}
	 */
	DomainReferenceBinding findFromElementExposingAttribute(String attributeName);
}
