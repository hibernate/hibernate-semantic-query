/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.orm.persister.common.internal;

import java.util.List;

import org.hibernate.orm.persister.common.spi.AbstractSingularAttribute;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.common.spi.JoinColumnMapping;
import org.hibernate.orm.persister.common.spi.JoinableOrmAttribute;
import org.hibernate.orm.persister.common.spi.ManagedTypeImplementor;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.persister.entity.spi.EntityReference;
import org.hibernate.orm.type.spi.EntityType;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.query.sqm.domain.SqmNavigable;
import org.hibernate.query.sqm.domain.SqmSingularAttributeEntity;


/**
 * @author Steve Ebersole
 */
public class SingularAttributeEntity
		extends AbstractSingularAttribute<EntityType>
		implements JoinableOrmAttribute, SqmSingularAttributeEntity, EntityReference {
	private final SingularAttributeClassification classification;
	private final List<Column> columns;

	private List<JoinColumnMapping> joinColumnMappings;

	public SingularAttributeEntity(
			ManagedTypeImplementor declaringType,
			String name,
			PropertyAccess propertyAccess,
			Disposition disposition,
			SingularAttributeClassification classification,
			EntityType ormType,
			List<Column> columns) {
		super( declaringType, name, propertyAccess, ormType, disposition, true );
		this.classification = classification;

		// columns should be the rhs columns I believe.
		//		todo : add an assertion based on whatever this should be...
		this.columns = columns;
	}

	@Override
	public SqmNavigable findNavigable(String navigableName) {
		return getEntityPersister().findNavigable( navigableName );
	}

	@Override
	public EntityPersister getEntityPersister() {
		return getOrmType().getEntityPersister();
	}

	public String getEntityName() {
		return getEntityPersister().getEntityName();
	}

	@Override
	public String getJpaEntityName() {
		return getEntityPersister().getJpaEntityName();
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return classification;
	}

	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public String asLoggableText() {
		return "SingularAttributeEntity([" + getAttributeTypeClassification().name() + "] " +
				getSource().asLoggableText() + '.' + getAttributeName() +
				")";
	}

	@Override
	public String toString() {
		return asLoggableText();
	}

	@Override
	public List<JoinColumnMapping> getJoinColumnMappings() {
		if ( joinColumnMappings == null ) {
			this.joinColumnMappings = getSource().resolveJoinColumnMappings( this );
		}
		return joinColumnMappings;
	}

	@Override
	public EntityReference getType() {
		// this is the JPA type
		return getEntityPersister();
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return getAttributeTypeClassification() == SingularAttributeClassification.ONE_TO_ONE
				? PersistentAttributeType.ONE_TO_ONE
				: PersistentAttributeType.MANY_TO_ONE;
	}

	@Override
	public boolean isAssociation() {
		return true;
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.ENTITY;
	}

	@Override
	public EntityType getExportedDomainType() {
		return getOrmType();
	}
}
