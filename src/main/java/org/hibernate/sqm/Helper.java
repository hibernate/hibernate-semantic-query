/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static <T> List<T> toList(T[] values) {
		if ( values == null ) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableList( Arrays.asList( values ) );
	}

	public static <T> List<T> toExpandableList(T[] values) {
		if ( values == null ) {
			return new ArrayList<T>();
		}
		return Arrays.asList( values );
	}

	public static boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}

	public static boolean isNotEmpty(String string) {
		return !isEmpty( string );
	}
}
