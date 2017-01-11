/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.sql.spi;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.orm.type.descriptor.java.spi.BasicJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.JdbcValueBinder;
import org.hibernate.orm.type.descriptor.spi.JdbcValueExtractor;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;
import org.hibernate.orm.type.descriptor.sql.internal.JdbcLiteralFormatterNumericData;
import org.hibernate.orm.type.spi.JdbcLiteralFormatter;
import org.hibernate.orm.type.spi.TypeConfiguration;

/**
 * Descriptor for {@link Types#REAL REAL} handling.
 *
 * @author Steve Ebersole
 */
public class RealSqlDescriptor implements SqlTypeDescriptor {
	public static final RealSqlDescriptor INSTANCE = new RealSqlDescriptor();

	public RealSqlDescriptor() {
	}

	@Override
	public int getSqlType() {
		return Types.REAL;
	}

	@Override
	public boolean canBeRemapped() {
		return true;
	}

	@Override
	public BasicJavaTypeDescriptor getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
		return (BasicJavaTypeDescriptor) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( Float.class );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaTypeDescriptor<T> javaTypeDescriptor) {
		return new JdbcLiteralFormatterNumericData( javaTypeDescriptor, Float.class );
	}

	@Override
	public <X> JdbcValueBinder<X> getBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
		return new StandardJdbcValueBinder<X>( javaTypeDescriptor, this ) {
			@Override
			protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
				st.setFloat( index, javaTypeDescriptor.unwrap( value, Float.class, options ) );
			}

			@Override
			protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
					throws SQLException {
				st.setFloat( name, javaTypeDescriptor.unwrap( value, Float.class, options ) );
			}
		};
	}

	@Override
	public <X> JdbcValueExtractor<X> getExtractor(final JavaTypeDescriptor<X> javaTypeDescriptor) {
		return new StandardJdbcValueExtractor<X>( javaTypeDescriptor, this ) {
			@Override
			protected X doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( rs.getFloat( name ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( statement.getFloat( index ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( statement.getFloat( name ), options );
			}
		};
	}
}
