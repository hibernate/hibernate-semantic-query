/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.entity.internal;

import org.hibernate.orm.persister.common.internal.SingularAttributeEmbedded;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.persister.entity.spi.IdentifierDescriptorAggregatedEmbedded;
import org.hibernate.orm.type.spi.EmbeddedType;
import org.hibernate.sqm.domain.type.SqmDomainType;

/**
 * @author Steve Ebersole
 */
public class IdentifierDescriptorAggregatedEmbeddedImpl implements IdentifierDescriptorAggregatedEmbedded {
	private final SingularAttributeEmbedded embeddedIdAttribute;

	public IdentifierDescriptorAggregatedEmbeddedImpl(SingularAttributeEmbedded embeddedIdAttribute) {
		this.embeddedIdAttribute = embeddedIdAttribute;
	}

	@Override
	public EmbeddedType getIdType() {
		return embeddedIdAttribute.getOrmType();
	}

	@Override
	public String getReferableAttributeName() {
		return embeddedIdAttribute.getName();
	}

	@Override
	public SingularAttributeEmbedded getIdAttribute() {
		return embeddedIdAttribute;
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return embeddedIdAttribute.getExportedDomainType();
	}

	@Override
	public String asLoggableText() {
		return embeddedIdAttribute.asLoggableText();
	}

	@Override
	public EntityPersister getSource() {
		return (EntityPersister) embeddedIdAttribute.getSource();
	}

	@Override
	public String getNavigableName() {
		return embeddedIdAttribute.getNavigableName();
	}

	@Override
	public String getTypeName() {
		return embeddedIdAttribute.getTypeName();
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.EMBEDDABLE;
	}

	@Override
	public Class getJavaType() {
		return embeddedIdAttribute.getJavaType();
	}
}
