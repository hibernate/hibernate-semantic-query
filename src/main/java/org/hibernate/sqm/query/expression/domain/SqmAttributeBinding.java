/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.domain.SqmAttribute;
import org.hibernate.sqm.query.from.SqmFromExporter;

/**
 * Models the binding of a persistent attribute of the domain model.
 *
 * @author Steve Ebersole
 */
public interface SqmAttributeBinding extends SqmNavigableBinding, SqmFromExporter {
	@Override
	SqmAttribute getBoundNavigable();

}
