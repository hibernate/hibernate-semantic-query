/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.query.expression.Expression;

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
}
