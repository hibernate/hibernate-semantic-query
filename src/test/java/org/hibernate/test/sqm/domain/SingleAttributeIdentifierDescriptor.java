/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.SingularAttributeDescriptor.SingularAttributeClassification;

/**
 * @author Steve Ebersole
 */
public class SingleAttributeIdentifierDescriptor implements IdentifierDescriptorSingleAttribute {
	private final SingularAttribute idAttribute;

	public SingleAttributeIdentifierDescriptor(
			IdentifiableType entityType,
			String idAttributeName,
			Type idType) {
		this.idAttribute = new SingularAttributeImpl(
				entityType,
				idAttributeName,
				idType instanceof EmbeddableType
						? SingularAttributeClassification.EMBEDDED
						: SingularAttributeClassification.BASIC,
				idType
		);
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
	public String getReferableAttributeName() {
		return idAttribute.getAttributeName();
	}

	@Override
	public SingularAttribute getIdAttribute() {
		return idAttribute;
	}
}
