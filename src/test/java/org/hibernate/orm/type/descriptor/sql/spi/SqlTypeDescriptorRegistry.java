/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.sql.spi;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.orm.type.descriptor.java.spi.BasicJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.JdbcValueBinder;
import org.hibernate.orm.type.descriptor.spi.JdbcValueExtractor;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;
import org.hibernate.orm.type.spi.JdbcLiteralFormatter;
import org.hibernate.orm.type.spi.TypeConfiguration;

import org.jboss.logging.Logger;

/**
 * Basically a map from JDBC type code (int) -> {@link SqlTypeDescriptor}
 *
 * @author Steve Ebersole
 */
public class SqlTypeDescriptorRegistry {
	private static final Logger log = Logger.getLogger( SqlTypeDescriptorRegistry.class );

	private ConcurrentHashMap<Integer, SqlTypeDescriptor> descriptorMap = new ConcurrentHashMap<>();

	public SqlTypeDescriptorRegistry(TypeConfiguration typeConfiguration) {
		addDescriptor( BooleanSqlDescriptor.INSTANCE );

		addDescriptor( BitSqlDescriptor.INSTANCE );
		addDescriptor( BigIntSqlDescriptor.INSTANCE );
		addDescriptor( DecimalSqlDescriptor.INSTANCE );
		addDescriptor( DoubleSqlDescriptor.INSTANCE );
		addDescriptor( FloatSqlDescriptor.INSTANCE );
		addDescriptor( IntegerSqlDescriptor.INSTANCE );
		addDescriptor( NumericSqlDescriptor.INSTANCE );
		addDescriptor( RealSqlDescriptor.INSTANCE );
		addDescriptor( SmallIntSqlDescriptor.INSTANCE );
		addDescriptor( TinyIntSqlDescriptor.INSTANCE );

		addDescriptor( DateSqlDescriptor.INSTANCE );
		addDescriptor( TimestampSqlDescriptor.INSTANCE );
		addDescriptor( TimeSqlDescriptor.INSTANCE );

		addDescriptor( BinarySqlDescriptor.INSTANCE );
		addDescriptor( VarbinarySqlDescriptor.INSTANCE );
		addDescriptor( LongVarbinarySqlDescriptor.INSTANCE );
		addDescriptor( BlobSqlDescriptor.DEFAULT );

		addDescriptor( CharSqlDescriptor.INSTANCE );
		addDescriptor( VarcharSqlDescriptor.INSTANCE );
		addDescriptor( LongVarcharSqlDescriptor.INSTANCE );
		addDescriptor( ClobSqlDescriptor.DEFAULT );

		addDescriptor( NCharSqlDescriptor.INSTANCE );
		addDescriptor( NVarcharSqlDescriptor.INSTANCE );
		addDescriptor( LongNVarcharSqlDescriptor.INSTANCE );
		addDescriptor( NClobSqlDescriptor.DEFAULT );
	}

	public void addDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
		descriptorMap.put( sqlTypeDescriptor.getSqlType(), sqlTypeDescriptor );
	}

	public SqlTypeDescriptor getDescriptor(int jdbcTypeCode) {
		SqlTypeDescriptor descriptor = descriptorMap.get( Integer.valueOf( jdbcTypeCode ) );
		if ( descriptor != null ) {
			return descriptor;
		}

		if ( JdbcTypeNameMapper.isStandardTypeCode( jdbcTypeCode ) ) {
			log.debugf(
					"A standard JDBC type code [%s] was not defined in SqlTypeDescriptorRegistry",
					jdbcTypeCode
			);
		}

		// see if the typecode is part of a known type family...
		JdbcTypeFamilyInformation.Family family = JdbcTypeFamilyInformation.INSTANCE.locateJdbcTypeFamilyByTypeCode( jdbcTypeCode );
		if ( family != null ) {
			for ( int potentialAlternateTypeCode : family.getTypeCodes() ) {
				if ( potentialAlternateTypeCode != jdbcTypeCode ) {
					final SqlTypeDescriptor potentialAlternateDescriptor = descriptorMap.get( Integer.valueOf( potentialAlternateTypeCode ) );
					if ( potentialAlternateDescriptor != null ) {
						// todo : add a SqlTypeDescriptor.canBeAssignedFrom method...
						return potentialAlternateDescriptor;
					}

					if ( JdbcTypeNameMapper.isStandardTypeCode( potentialAlternateTypeCode ) ) {
						log.debugf(
								"A standard JDBC type code [%s] was not defined in SqlTypeDescriptorRegistry",
								potentialAlternateTypeCode
						);
					}
				}
			}
		}

		// finally, create a new descriptor mapping to getObject/setObject for this type code...
		final ObjectSqlTypeDescriptor fallBackDescriptor = new ObjectSqlTypeDescriptor( jdbcTypeCode );
		addDescriptor( fallBackDescriptor );
		return fallBackDescriptor;
	}

	public static class ObjectSqlTypeDescriptor implements SqlTypeDescriptor {
		private final int jdbcTypeCode;

		public ObjectSqlTypeDescriptor(int jdbcTypeCode) {
			this.jdbcTypeCode = jdbcTypeCode;
		}

		@Override
		public int getSqlType() {
			return jdbcTypeCode;
		}

		@Override
		public BasicJavaTypeDescriptor getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			throw new UnsupportedOperationException( "No recommended Java-type mapping known for JDBC type code [" + jdbcTypeCode + "]" );
		}

		@Override
		public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaTypeDescriptor<T> javaTypeDescriptor) {
			// obviously no literal support here :)
			return null;
		}

		@Override
		public boolean canBeRemapped() {
			return true;
		}

		@Override
		public <X> JdbcValueBinder<X> getBinder(JavaTypeDescriptor<X> javaTypeDescriptor) {
			if ( javaTypeDescriptor.getJavaType() != null
					&& Serializable.class.isAssignableFrom( javaTypeDescriptor.getJavaType() ) ) {
				return VarbinarySqlDescriptor.INSTANCE.getBinder( javaTypeDescriptor );
			}

			return new StandardJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options)
						throws SQLException {
					st.setObject( index, value, jdbcTypeCode );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
						throws SQLException {
					st.setObject( name, value, jdbcTypeCode );
				}
			};
		}

		@Override
		@SuppressWarnings("unchecked")
		public JdbcValueExtractor getExtractor(JavaTypeDescriptor javaTypeDescriptor) {
			if ( javaTypeDescriptor.getJavaType() != null
					&& Serializable.class.isAssignableFrom( javaTypeDescriptor.getJavaType() ) ) {
				return VarbinarySqlDescriptor.INSTANCE.getExtractor( javaTypeDescriptor );
			}

			return new StandardJdbcValueExtractor( javaTypeDescriptor, this ) {
				@Override
				protected Object doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
					return rs.getObject( name );
				}

				@Override
				protected Object doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
					return statement.getObject( index );
				}

				@Override
				protected Object doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
					return statement.getObject( name );
				}
			};
		}
	}
}
