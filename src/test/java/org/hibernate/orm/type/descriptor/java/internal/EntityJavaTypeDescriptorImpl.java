/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.internal;

import java.util.Comparator;

import org.hibernate.orm.type.descriptor.java.spi.EntityJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.ManagedJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;
import org.hibernate.orm.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class EntityJavaTypeDescriptorImpl extends AbstractIdentifiableJavaDescriptor implements EntityJavaTypeDescriptor {
	private final String entityName;

	public EntityJavaTypeDescriptorImpl(
			String typeName,
			String entityName,
			Class javaType,
			ManagedJavaTypeDescriptor superTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator) {
		super(
				typeName,
				javaType,
				superTypeDescriptor,
				mutabilityPlan,
				comparator
		);
		this.entityName = entityName;
	}

	@Override
	public String getEntityName() {
		return getTypeConfiguration().findEntityPersister( entityName ).getEntityName();
	}

	@Override
	public String getJpaEntityName() {
		return getTypeConfiguration().findEntityPersister( entityName ).getJpaEntityName();
	}

	@Override
	public int extractHashCode(Object value) {
		return value.hashCode();
	}

	@Override
	public boolean areEqual(Object one, Object another) {
		return false;
	}

	@Override
	public String extractLoggableRepresentation(Object entity) {
		return "Entity(" + entity + ")";
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		throw new UnsupportedOperationException( "SqlTypeDescriptor must be specified for EntityType" );
	}

	@Override
	public String toString(Object value) {
		throw new UnsupportedOperationException(  );
	}

	@Override
	public Object fromString(String string) {
		throw new UnsupportedOperationException( "Entity type cannot be read from String" );
	}

	@Override
	public Object wrap(Object value, WrapperOptions options) {
		throw new UnsupportedOperationException(  );
	}

	@Override
	public Object unwrap(Object value, Class type, WrapperOptions options) {
		throw new UnsupportedOperationException(  );
	}
}
