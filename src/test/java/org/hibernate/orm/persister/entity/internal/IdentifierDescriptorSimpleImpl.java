/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.entity.internal;

import java.util.List;

import javax.persistence.metamodel.Type;

import org.hibernate.orm.persister.common.internal.OrmSingularAttributeBasic;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.common.spi.OrmNavigableSource;
import org.hibernate.orm.persister.common.spi.OrmSingularAttribute;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.persister.entity.spi.IdentifiableTypeImplementor;
import org.hibernate.orm.persister.entity.spi.IdentifierDescriptorSimple;
import org.hibernate.orm.type.spi.BasicType;
import org.hibernate.property.access.internal.PropertyAccessStrategyMapImpl;
import org.hibernate.sqm.domain.type.SqmDomainType;

/**
 * @author Steve Ebersole
 */
public class IdentifierDescriptorSimpleImpl implements IdentifierDescriptorSimple {
	private final OrmSingularAttributeBasic idAttribute;

	public IdentifierDescriptorSimpleImpl(
			IdentifiableTypeImplementor declaringType,
			String idAttributeName,
			BasicType idType,
			List<Column> columns) {
		this.idAttribute = new OrmSingularAttributeBasic(
				declaringType,
				idAttributeName,
				// for now we just build the EntityMode.MAP accessor for testing
				PropertyAccessStrategyMapImpl.INSTANCE.buildPropertyAccess( null, idAttributeName ),
				idType,
				OrmSingularAttribute.Disposition.ID,
				null,
				columns
		);
	}

	@Override
	public BasicType getIdType() {
		return idAttribute.getOrmType();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return true;
	}

	@Override
	public OrmSingularAttributeBasic getIdAttribute() {
		return idAttribute;
	}

	@Override
	public String getReferableAttributeName() {
		return idAttribute.getName();
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return idAttribute.getOrmType();
	}

	@Override
	public String asLoggableText() {
		return "<simple-id>";
	}

	@Override
	public EntityPersister getSource() {
		return (EntityPersister) idAttribute.getSource();
	}

	@Override
	public String getNavigableName() {
		return idAttribute.getName();
	}

	@Override
	public String getTypeName() {
		return idAttribute.getTypeName();
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.BASIC;
	}

	@Override
	public Class getJavaType() {
		return idAttribute.getJavaType();
	}
}
