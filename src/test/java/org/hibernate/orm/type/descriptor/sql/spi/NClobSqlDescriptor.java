/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.sql.spi;

import java.sql.CallableStatement;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.engine.jdbc.CharacterStream;

import org.hibernate.orm.type.descriptor.java.spi.BasicJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.JdbcValueBinder;
import org.hibernate.orm.type.descriptor.spi.JdbcValueExtractor;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;
import org.hibernate.orm.type.spi.JdbcLiteralFormatter;
import org.hibernate.orm.type.spi.TypeConfiguration;

/**
 * Descriptor for {@link Types#NCLOB NCLOB} handling.
 *
 * @author Steve Ebersole
 * @author Gail Badner
 */
public abstract class NClobSqlDescriptor implements SqlTypeDescriptor {
	@Override
	public int getSqlType() {
		return Types.NCLOB;
	}

	@Override
	public boolean canBeRemapped() {
		return true;
	}

	@Override
	public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaTypeDescriptor<T> javaTypeDescriptor) {
		// literal values for (N)CLOB data is not supported.
		return null;
	}

	@Override
	public <X> JdbcValueExtractor<X> getExtractor(final JavaTypeDescriptor<X> javaTypeDescriptor) {
		return new StandardJdbcValueExtractor<X>( javaTypeDescriptor, this ) {
			@Override
			protected X doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( rs.getNClob( name ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, int index, WrapperOptions options)
					throws SQLException {
				return javaTypeDescriptor.wrap( statement.getNClob( index ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, String name, WrapperOptions options)
					throws SQLException {
				return javaTypeDescriptor.wrap( statement.getNClob( name ), options );
			}
		};
	}

	protected abstract <X> StandardJdbcValueBinder<X> getNClobBinder(JavaTypeDescriptor<X> javaTypeDescriptor);

	@Override
	public <X> JdbcValueBinder<X> getBinder(JavaTypeDescriptor<X> javaTypeDescriptor) {
		return getNClobBinder( javaTypeDescriptor );
	}


	public static final NClobSqlDescriptor DEFAULT = new NClobSqlDescriptor() {
		@Override
		public BasicJavaTypeDescriptor getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return (BasicJavaTypeDescriptor) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( NClob.class );
		}

		@Override
		public <X> StandardJdbcValueBinder<X> getNClobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new StandardJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options)
						throws SQLException {
					if ( options.useStreamForLobBinding() ) {
						STREAM_BINDING.getNClobBinder( javaTypeDescriptor ).doBind( st, value, index, options );
					}
					else {
						NCLOB_BINDING.getNClobBinder( javaTypeDescriptor ).doBind( st, value, index, options );
					}
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
						throws SQLException {
					if ( options.useStreamForLobBinding() ) {
						STREAM_BINDING.getNClobBinder( javaTypeDescriptor ).doBind( st, value, name, options );
					}
					else {
						NCLOB_BINDING.getNClobBinder( javaTypeDescriptor ).doBind( st, value, name, options );
					}
				}
			};
		}
	};

	public static final NClobSqlDescriptor NCLOB_BINDING = new NClobSqlDescriptor() {
		@Override
		public BasicJavaTypeDescriptor getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return (BasicJavaTypeDescriptor) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( NClob.class );
		}

		@Override
		public <X> StandardJdbcValueBinder<X> getNClobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new StandardJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options)
						throws SQLException {
					st.setNClob( index, javaTypeDescriptor.unwrap( value, NClob.class, options ) );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
						throws SQLException {
					st.setNClob( name, javaTypeDescriptor.unwrap( value, NClob.class, options ) );
				}
			};
		}
	};

	public static final NClobSqlDescriptor STREAM_BINDING = new NClobSqlDescriptor() {
		@Override
		public BasicJavaTypeDescriptor getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return null;
		}

		@Override
		public <X> StandardJdbcValueBinder<X> getNClobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new StandardJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options)
						throws SQLException {
					final CharacterStream characterStream = javaTypeDescriptor.unwrap(
							value,
							CharacterStream.class,
							options
					);
					st.setCharacterStream( index, characterStream.asReader(), characterStream.getLength() );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
						throws SQLException {
					final CharacterStream characterStream = javaTypeDescriptor.unwrap(
							value,
							CharacterStream.class,
							options
					);
					st.setCharacterStream( name, characterStream.asReader(), characterStream.getLength() );
				}
			};
		}
	};
}
