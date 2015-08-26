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
package org.hibernate.sqm.path;

import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.query.from.FromElement;

/**
 * Represents an individual part in an attribute path expression.  For example,
 * given an HQL fragment like {@code select a.b.c from A a}, we have a path expression
 * of {@code a.b.c} which is made up of multiple parts:<ul>
 *     <li>{@code a}</li>
 *     <li>{@code a.b}</li>
 *     <li>{@code a.b.c}</li>
 * </ul>
 * This contract represents each of those parts individually.
 *
 * @author Steve Ebersole
 */
public interface AttributePathPart {
//	/**
//	 * Return the path which led to this source.
//	 *
//	 * @return The origination path.
//	 */
//	String getOriginationPathText();

	TypeDescriptor getTypeDescriptor();

	FromElement getUnderlyingFromElement();
}
