/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.order;

/**
 * @author Steve Ebersole
 */
public enum SqmSortOrder {
	ASCENDING,
	DESCENDING;

	public static SqmSortOrder interpret(String value) {
		if ( value == null ) {
			return null;
		}

		if ( value.equalsIgnoreCase( "ascending" ) || value.equalsIgnoreCase( "asc" ) ) {
			return ASCENDING;
		}

		if ( value.equalsIgnoreCase( "descending" ) || value.equalsIgnoreCase( "desc" ) ) {
			return DESCENDING;
		}

		throw new IllegalArgumentException( "Unknown sort order : " + value );
	}
}
