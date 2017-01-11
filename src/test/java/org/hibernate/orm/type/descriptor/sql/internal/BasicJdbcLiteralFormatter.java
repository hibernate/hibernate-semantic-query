/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.sql.internal;

import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;

/**
 * @author Steve Ebersole
 */
public abstract class BasicJdbcLiteralFormatter extends AbstractJdbcLiteralFormatter {
	public BasicJdbcLiteralFormatter(JavaTypeDescriptor javaTypeDescriptor) {
		super( javaTypeDescriptor );
	}

	@SuppressWarnings("unchecked")
	protected <T> T unwrap(Object value, Class<T> unwrapType, WrapperOptions wrapperOptions) {
		return (T) getJavaTypeDescriptor().unwrap( value, unwrapType, wrapperOptions );
	}
}
