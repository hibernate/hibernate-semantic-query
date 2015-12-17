/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.path;

import org.hibernate.sqm.domain.Attribute;

/**
 * Represents an Attribute binding (reference) in the query.
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

	/**
	 * Obtain the source (lhs) of this AttributeBinding
	 *
	 * @return The lhs of this AttributeBinding
	 */
	AttributeBindingSource getAttributeBindingSource();
}
