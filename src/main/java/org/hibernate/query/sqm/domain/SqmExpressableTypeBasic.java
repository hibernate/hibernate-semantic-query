/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

import org.hibernate.query.sqm.domain.type.SqmDomainTypeBasic;

/**
 * A SqmExpressableType which resolves to a basic type.
 *
 * @author Steve Ebersole
 *
 * @deprecated need some form of "basic reference" to cover<ul>
 *     <i>basic attribute</i>
 *     <li>basic element</li>
 *     <li>basic index</li>
 *     <li>?</li>
 * </ul>
 */
@Deprecated
public interface SqmExpressableTypeBasic<T> extends SqmExpressableType<T> {
	@Override
	SqmDomainTypeBasic<T> getExportedDomainType();
}
