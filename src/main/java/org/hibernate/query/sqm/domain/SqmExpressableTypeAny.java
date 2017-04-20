/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

import org.hibernate.query.sqm.domain.type.SqmDomainTypeAny;

/**
 * @author Steve Ebersole
 *
 * @deprecated need some form of "any reference"
 */
@Deprecated
public interface SqmExpressableTypeAny extends SqmExpressableType {
	@Override
	SqmDomainTypeAny getExportedDomainType();
}
