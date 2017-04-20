/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.internal;

import java.util.Comparator;

import org.hibernate.orm.type.descriptor.java.spi.EmbeddableJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.ManagedJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;
import org.hibernate.orm.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.query.sqm.NotYetImplementedException;

/**
 * @author Steve Ebersole
 */
public class EmbeddableJavaTypeDescriptorImpl extends AbstractManagedJavaDescriptor implements EmbeddableJavaTypeDescriptor {
	public EmbeddableJavaTypeDescriptorImpl(
			String typeName,
			Class javaType,
			ManagedJavaTypeDescriptor superTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator) {
		super( typeName, javaType, superTypeDescriptor, mutabilityPlan, comparator );
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		// none
		return null;
	}

	@Override
	public int extractHashCode(Object value) {
		return value.hashCode();
	}

	@Override
	public boolean areEqual(Object one, Object another) {
		return one == another
				|| ( one != null && one.equals( another ) );
	}

	@Override
	public String extractLoggableRepresentation(Object value) {
		return "{composite-value=" + value + "}";
	}

	@Override
	public String toString(Object value) {
		return null;
	}

	@Override
	public Object fromString(String string) {
		return null;
	}

	@Override
	public Object wrap(Object value, WrapperOptions options) {
		throw new NotYetImplementedException(  );
	}

	@Override
	public Object unwrap(Object value, Class type, WrapperOptions options) {
		throw new NotYetImplementedException(  );
	}
}
