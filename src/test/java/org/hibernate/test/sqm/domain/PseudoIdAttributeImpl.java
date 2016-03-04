/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
class PseudoIdAttributeImpl extends SingularAttributeImpl {
	public PseudoIdAttributeImpl(ManagedType declaringType, Type type, Classification classification) {
		super( declaringType, "id", classification, type );
	}
}
