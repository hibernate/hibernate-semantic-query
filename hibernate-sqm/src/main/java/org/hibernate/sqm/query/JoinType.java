/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents a canonical join type.
 * <p/>
 * Note that currently HQL really only supports inner and left outer joins
 * (though cross joins can also be achieved).  This is because joins in HQL
 * are always defined in relation to a mapped association.  However, when we
 * start allowing users to specify ad-hoc joins this may need to change to
 * allow the full spectrum of join types.  Thus the others are provided here
 * currently just for completeness and for future expansion.
 *
 * @author Steve Ebersole
 */
public enum JoinType {
	/**
	 * Represents an inner join.
	 */
	INNER( "inner" ),

	/**
	 * Represents a left outer join.
	 */
	LEFT( "left outer" ),

	/**
	 * Represents a right outer join.
	 */
	RIGHT( "right outer" ),

	/**
	 * Represents a cross join (aka a cartesian product).
	 */
	CROSS( "cross" ),

	/**
	 * Represents a full join.
	 */
	FULL( "full" );

	private final String text;

	private JoinType(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

	public String getText() {
		return text;
	}
}
