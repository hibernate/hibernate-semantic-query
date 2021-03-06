/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.internal;

import org.hibernate.orm.type.descriptor.java.spi.AbstractBasicTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;
import org.hibernate.orm.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeBasic;

/**
 * @author Steve Ebersole
 */
public class NonStandardBasicJavaTypeDescriptor<T> extends AbstractBasicTypeDescriptor<T> implements SqmDomainTypeBasic {
	public NonStandardBasicJavaTypeDescriptor(Class<T> type) {
		super( type );
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		// none
		return null;
	}

	@Override
	public String toString(T value) {
		return null;
	}

	@Override
	public T fromString(String string) {
		return null;
	}

	@Override
	public <X> X unwrap(T value, Class<X> type, WrapperOptions options) {
		return null;
	}

	@Override
	public <X> T wrap(X value, WrapperOptions options) {
		return null;
	}

	@Override
	public String asLoggableText() {
		return "{non-standard-basic-type(" + getJavaType().getName() + "}";
	}

	@Override
	public SqmDomainTypeBasic getExportedDomainType() {
		return this;
	}
}
