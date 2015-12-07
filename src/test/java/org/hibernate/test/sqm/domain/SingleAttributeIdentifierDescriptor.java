/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.IdentifierDescriptorSingleAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class SingleAttributeIdentifierDescriptor implements IdentifierDescriptorSingleAttribute {
	private final SingularAttribute idAttribute;

	public SingleAttributeIdentifierDescriptor(SingularAttribute idAttribute) {
		this.idAttribute = idAttribute;
	}

	@Override
	public Type getIdType() {
		return idAttribute.getBoundType();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return true;
	}

	@Override
	public SingularAttribute getIdAttribute() {
		return idAttribute;
	}
}
