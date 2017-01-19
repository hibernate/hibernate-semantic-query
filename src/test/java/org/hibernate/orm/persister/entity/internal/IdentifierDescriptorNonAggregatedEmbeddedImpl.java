/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.entity.internal;

import java.util.Collections;
import java.util.Set;

import org.hibernate.orm.persister.common.spi.SingularAttribute;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.persister.entity.spi.IdClassDescriptor;
import org.hibernate.orm.persister.entity.spi.IdentifierDescriptorNonAggregatedEmbedded;
import org.hibernate.orm.type.spi.EmbeddedType;
import org.hibernate.orm.type.spi.Type;
import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.domain.type.SqmDomainTypeEmbeddable;

/**
 * @author Steve Ebersole
 */
public class IdentifierDescriptorNonAggregatedEmbeddedImpl implements IdentifierDescriptorNonAggregatedEmbedded {
	private final EntityPersister entityPersister;
	private final EmbeddedType idType;

	// NOTE : JPA requires these to have an IdClass, so we work with that assumption here for testing
	private final IdClassDescriptor idClassDescriptor;


	public IdentifierDescriptorNonAggregatedEmbeddedImpl(
			EntityPersister entityPersister,
			EmbeddedType idType,
			EmbeddedType idClassType) {
		this.entityPersister = entityPersister;
		this.idClassDescriptor = new IdClassDescriptorImpl( idClassType );
		this.idType = idType;
	}

	@Override
	public Set<SingularAttribute> getIdentifierAttributes() {
		throw new NotYetImplementedException(  );
	}

	@Override
	public Type getIdType() {
		return idClassDescriptor.getType();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return false;
	}

	@Override
	public String getReferableAttributeName() {
		return "<id>";
	}

	@Override
	public IdClassDescriptor getIdClassDescriptor() {
		return idClassDescriptor;
	}

	@Override
	public SqmDomainTypeEmbeddable getExportedDomainType() {
		return idType;
	}

	@Override
	public String asLoggableText() {
		return entityPersister.asLoggableText() + ".<id>";
	}

	@Override
	public EntityPersister getSource() {
		return entityPersister;
	}

	@Override
	public String getNavigableName() {
		return "<id>";
	}

	@Override
	public String getTypeName() {
		return entityPersister.getTypeName();
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.EMBEDDABLE;
	}

	@Override
	public Class getJavaType() {
		return idType.getJavaTypeDescriptor().getJavaType();
	}

	@Override
	public SqmNavigable findNavigable(String navigableName) {
		return idType.getEmbeddableMapper().findNavigable( navigableName );
	}

	static class IdClassDescriptorImpl implements IdClassDescriptor {
		private final EmbeddedType idClassType;
		private final Set<SingularAttribute> idClassAttributes = Collections.emptySet();

		public IdClassDescriptorImpl(EmbeddedType idClassType) {
			this.idClassType = idClassType;
		}

		@Override
		public Type getType() {
			return idClassType;
		}

		@Override
		public Set<SingularAttribute> getAttributes() {
			return idClassAttributes;
		}
	}
}
