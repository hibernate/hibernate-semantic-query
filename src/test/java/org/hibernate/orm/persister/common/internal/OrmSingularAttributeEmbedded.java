/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.orm.persister.common.internal;

import java.util.Collections;
import java.util.List;
import javax.persistence.metamodel.Type;

import org.hibernate.orm.persister.common.spi.AbstractOrmSingularAttribute;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.common.spi.JoinColumnMapping;
import org.hibernate.orm.persister.common.spi.JoinableOrmAttribute;
import org.hibernate.orm.persister.common.spi.ManagedTypeImplementor;
import org.hibernate.orm.persister.common.spi.OrmSingularAttribute;
import org.hibernate.orm.persister.embeddable.internal.EmbeddableMapperImpl;
import org.hibernate.orm.persister.embeddable.spi.EmbeddableMapper;
import org.hibernate.orm.type.spi.EmbeddedType;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.domain.SqmSingularAttributeEmbedded;

/**
 * @author Steve Ebersole
 */
public class OrmSingularAttributeEmbedded
		extends AbstractOrmSingularAttribute<EmbeddedType>
		implements OrmSingularAttribute, CompositeReference, JoinableOrmAttribute, SqmSingularAttributeEmbedded {
	private final EmbeddableMapper embeddablePersister;

	public OrmSingularAttributeEmbedded(
			ManagedTypeImplementor declaringType,
			String attributeName,
			PropertyAccess propertyAccess,
			Disposition disposition,
			EmbeddableMapper embeddablePersister) {
		super( declaringType, attributeName, propertyAccess, embeddablePersister.getOrmType(), disposition, true );
		this.embeddablePersister = embeddablePersister;
	}

	public EmbeddableMapper getEmbeddablePersister() {
		return embeddablePersister;
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.EMBEDDED;
	}

	@Override
	public List<Column> getColumns() {
		return embeddablePersister.collectColumns();
	}

	@Override
	public String asLoggableText() {
		return toString();
	}

	@Override
	public List<JoinColumnMapping> getJoinColumnMappings() {
		// there are no columns involved in a join to an embedded/composite attribute
		return Collections.emptyList();
	}

	@Override
	public Type getType() {
		return getEmbeddablePersister();
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return PersistentAttributeType.EMBEDDED;
	}

	@Override
	public boolean isAssociation() {
		return false;
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.EMBEDDABLE;
	}

	@Override
	public SqmNavigable findNavigable(String navigableName) {
		return getEmbeddablePersister().findNavigable( navigableName );
	}

	@Override
	public EmbeddedType getExportedDomainType() {
		return getOrmType();
	}
}
