/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.orm.persister.common.internal;

import java.util.List;
import java.util.Optional;
import javax.persistence.AttributeConverter;
import javax.persistence.metamodel.Type;

import org.hibernate.orm.persister.common.spi.AbstractOrmSingularAttribute;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.common.spi.ConvertibleOrmNavigable;
import org.hibernate.orm.persister.common.spi.ManagedTypeImplementor;
import org.hibernate.orm.type.spi.BasicType;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.sqm.domain.SqmExpressableTypeBasic;

/**
 * @author Steve Ebersole
 */
public class OrmSingularAttributeBasic
		extends AbstractOrmSingularAttribute<BasicType>
		implements ConvertibleOrmNavigable, SqmExpressableTypeBasic {
	private final List<Column> columns;
	private final AttributeConverter attributeConverter;

	public OrmSingularAttributeBasic(
			ManagedTypeImplementor declaringType,
			String name,
			PropertyAccess propertyAccess,
			BasicType ormType,
			Disposition disposition,
			AttributeConverter attributeConverter,
			List<Column> columns) {
		super( declaringType, name, propertyAccess, ormType, disposition, true );
		this.attributeConverter = attributeConverter;
		this.columns = columns;
	}

	@Override
	public BasicType getExportedDomainType() {
		return getOrmType();
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.BASIC;
	}

	@Override
	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public String asLoggableText() {
		return "SingularAttributeBasic(" + getSource().asLoggableText() + '.' + getAttributeName() + ')';
	}

	@Override
	public Optional<AttributeConverter> getAttributeConverter() {
		return Optional.of( attributeConverter );
	}

	@Override
	public String getTypeName() {
		return getOrmType().getTypeName();
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return PersistentAttributeType.BASIC;
	}

	@Override
	public boolean isAssociation() {
		return false;
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.BASIC;
	}

	@Override
	public Type getType() {
		return getOrmType();
	}
}
