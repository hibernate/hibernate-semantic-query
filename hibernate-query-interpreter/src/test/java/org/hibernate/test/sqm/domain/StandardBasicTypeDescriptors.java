/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author Steve Ebersole
 */
public class StandardBasicTypeDescriptors {
	/**
	 * Singleton access
	 */
	public static final StandardBasicTypeDescriptors INSTANCE = new StandardBasicTypeDescriptors();

	private StandardBasicTypeDescriptors() {
	}

	public final BasicTypeImpl NULL = new BasicTypeImpl( Void.class );

	public final BasicTypeImpl BOOLEAN = new BasicTypeImpl( Boolean.class );

	public final BasicTypeImpl CHAR = new BasicTypeImpl( Character.class );
	public final BasicTypeImpl STRING = new BasicTypeImpl( String.class );

	public final BasicTypeImpl BYTE = new BasicTypeImpl( Byte.class );
	public final BasicTypeImpl SHORT = new BasicTypeImpl( Short.class );
	public final BasicTypeImpl INTEGER = new BasicTypeImpl( Integer.class );
	public final BasicTypeImpl LONG = new BasicTypeImpl( Long.class );
	public final BasicTypeImpl BIG_INTEGER = new BasicTypeImpl( BigInteger.class );
	public final BasicTypeImpl FLOAT = new BasicTypeImpl( Float.class );
	public final BasicTypeImpl DOUBLE = new BasicTypeImpl( Double.class );
	public final BasicTypeImpl BIG_DECIMAL = new BasicTypeImpl( BigDecimal.class );

	public final BasicTypeImpl UUID = new BasicTypeImpl( java.util.UUID.class );

	public final BasicTypeImpl BLOB = new BasicTypeImpl( Blob.class );
	public final BasicTypeImpl CLOB = new BasicTypeImpl( Clob.class );
	public final BasicTypeImpl NCLOB = new BasicTypeImpl( NClob.class );

	public final BasicTypeImpl DATE = new BasicTypeImpl( java.util.Date.class );
	public final BasicTypeImpl JDBC_DATE = new BasicTypeImpl( Date.class );
	public final BasicTypeImpl JDBC_TIME = new BasicTypeImpl( Time.class );
	public final BasicTypeImpl JDBC_TIMESTAMP = new BasicTypeImpl( Timestamp.class );
	public final BasicTypeImpl CALENDAR = new BasicTypeImpl( Calendar.class );
	public final BasicTypeImpl TIMEZONE = new BasicTypeImpl( TimeZone.class );

	public final BasicTypeImpl CLASS = new BasicTypeImpl( Class.class );

	public final BasicTypeImpl LOCALE = new BasicTypeImpl( Locale.class );
	public final BasicTypeImpl CURRENCY = new BasicTypeImpl( Currency.class );

	public final BasicTypeImpl URL = new BasicTypeImpl( java.net.URL.class );

	public final BasicTypeImpl MAP = new BasicTypeImpl( Map.class );
	public final BasicTypeImpl LIST = new BasicTypeImpl( List.class );

}
