/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.internal;

import java.util.List;

import org.hibernate.orm.type.descriptor.java.spi.AbstractTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.BasicJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;
import org.hibernate.orm.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class ListJavaDescriptor extends AbstractTypeDescriptor<List> implements BasicJavaTypeDescriptor<List> {
	public ListJavaDescriptor() {
		super( List.class );
	}

	@Override
	public Class<List> getJavaType() {
		return List.class;
	}

	@Override
	public String getTypeName() {
		return getJavaType().getName();
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		// none
		return null;
	}

	@Override
	public int extractHashCode(List value) {
		return value.hashCode();
	}

	@Override
	public boolean areEqual(List one, List another) {
		return one == another
				|| ( one != null && one.equals( another ) );
	}

	@Override
	public String extractLoggableRepresentation(List value) {
		return "{list}";
	}

	@Override
	public String toString(List value) {
		return "{list}";
	}

	@Override
	public List fromString(String string) {
		throw new UnsupportedOperationException(  );
	}

	@Override
	public <X> X unwrap(List value, Class<X> type, WrapperOptions options) {
		throw new UnsupportedOperationException(  );
	}

	@Override
	public <X> List wrap(X value, WrapperOptions options) {
		throw new UnsupportedOperationException(  );
	}
}
