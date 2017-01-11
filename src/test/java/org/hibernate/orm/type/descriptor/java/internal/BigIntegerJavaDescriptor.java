/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;

import org.hibernate.orm.type.descriptor.java.spi.AbstractBasicTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;
import org.hibernate.orm.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * Descriptor for {@link BigInteger} handling.
 *
 * @author Steve Ebersole
 */
public class BigIntegerJavaDescriptor extends AbstractBasicTypeDescriptor<BigInteger> {
	public static final BigIntegerJavaDescriptor INSTANCE = new BigIntegerJavaDescriptor();

	public BigIntegerJavaDescriptor() {
		super( BigInteger.class );
	}

	@Override
	public String toString(BigInteger value) {
		return value.toString();
	}

	@Override
	public BigInteger fromString(String string) {
		return new BigInteger( string );
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		return context.getTypeConfiguration().getSqlTypeDescriptorRegistry().getDescriptor( Types.BIGINT );
	}

	@Override
	public int extractHashCode(BigInteger value) {
		return value.intValue();
	}

	@Override
	public boolean areEqual(BigInteger one, BigInteger another) {
		return one == another || ( one != null && another != null && one.compareTo( another ) == 0 );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <X> X unwrap(BigInteger value, Class<X> type, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( BigInteger.class.isAssignableFrom( type ) ) {
			return (X) value;
		}
		if ( BigDecimal.class.isAssignableFrom( type ) ) {
			return (X) new BigDecimal( value );
		}
		if ( Byte.class.isAssignableFrom( type ) ) {
			return (X) Byte.valueOf( value.byteValue() );
		}
		if ( Short.class.isAssignableFrom( type ) ) {
			return (X) Short.valueOf( value.shortValue() );
		}
		if ( Integer.class.isAssignableFrom( type ) ) {
			return (X) Integer.valueOf( value.intValue() );
		}
		if ( Long.class.isAssignableFrom( type ) ) {
			return (X) Long.valueOf( value.longValue() );
		}
		if ( Double.class.isAssignableFrom( type ) ) {
			return (X) Double.valueOf( value.doubleValue() );
		}
		if ( Float.class.isAssignableFrom( type ) ) {
			return (X) Float.valueOf( value.floatValue() );
		}
		throw unknownUnwrap( type );
	}

	@Override
	public <X> BigInteger wrap(X value, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( BigInteger.class.isInstance( value ) ) {
			return (BigInteger) value;
		}
		if ( BigDecimal.class.isInstance( value ) ) {
			return ( (BigDecimal) value ).toBigIntegerExact();
		}
		if ( Number.class.isInstance( value ) ) {
			return BigInteger.valueOf( ( (Number) value ).longValue() );
		}
		throw unknownWrap( value.getClass() );
	}
}
