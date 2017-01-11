/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.domain.SqmEntityIdentifier;

/**
 * @author Steve Ebersole
 */
public interface SqmEntityIdentifierBinding extends SqmNavigableBinding {
	@Override
	SqmEntityIdentifier getBoundNavigable();
}
