/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

import org.hibernate.query.sqm.domain.type.SqmDomainType;

/**
 * @author Steve Ebersole
 *
 * @deprecated todo (6.0) - proper replacement?
 */
@Deprecated
public interface SqmDomainTypeExporter<T> {
	SqmDomainType<T> getExportedDomainType();
}
