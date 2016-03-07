/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.EmbeddableType;
import org.hibernate.sqm.domain.IdentifierDescriptorSingleAttribute;
import org.hibernate.sqm.domain.SingularAttribute;

/**
 * @author Steve Ebersole
 */
public class AggregatedEmbeddedIdentifierDescriptor implements IdentifierDescriptorSingleAttribute {
	private final SingularAttribute embeddedIdAttribute;

	public AggregatedEmbeddedIdentifierDescriptor(SingularAttribute embeddedIdAttribute) {
		this.embeddedIdAttribute = embeddedIdAttribute;
	}

	@Override
	public EmbeddableType getIdType() {
		return (EmbeddableType) embeddedIdAttribute.getBoundType();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return true;
	}

	@Override
	public String getReferableAttributeName() {
		return embeddedIdAttribute.getName();
	}

	@Override
	public SingularAttribute getIdAttribute() {
		return embeddedIdAttribute;
	}
}
