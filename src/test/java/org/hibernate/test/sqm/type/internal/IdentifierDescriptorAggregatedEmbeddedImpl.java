/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;


import org.hibernate.test.sqm.type.spi.EmbeddableType;
import org.hibernate.test.sqm.type.spi.IdentifierDescriptorAggregatedEmbedded;
import org.hibernate.test.sqm.type.spi.IdentifierDescriptorSingleAttribute;
import org.hibernate.test.sqm.type.spi.SingularAttributeEmbedded;

/**
 * @author Steve Ebersole
 */
public class IdentifierDescriptorAggregatedEmbeddedImpl implements IdentifierDescriptorAggregatedEmbedded {
	private final SingularAttributeEmbedded embeddedIdAttribute;

	public IdentifierDescriptorAggregatedEmbeddedImpl(SingularAttributeEmbedded embeddedIdAttribute) {
		this.embeddedIdAttribute = embeddedIdAttribute;
	}

	@Override
	public EmbeddableType getIdType() {
		return embeddedIdAttribute.getType();
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
	public SingularAttributeEmbedded getIdAttribute() {
		return embeddedIdAttribute;
	}
}
