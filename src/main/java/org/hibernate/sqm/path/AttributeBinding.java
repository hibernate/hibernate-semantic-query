/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.path;

import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.query.from.SqmAttributeJoin;

/**
 * Represents an Attribute binding (reference) in the sqm.
 *
 * @author Steve Ebersole
 */
public interface AttributeBinding extends Binding {
	/**
	 * Obtain the bound/referenced attribute
	 *
	 * @return The bound/referenced attribute
	 */
	Attribute getBoundAttribute();

	Binding getLeftHandSide();

	void injectFromElementGeneratedForAttribute(SqmAttributeJoin join);
}
