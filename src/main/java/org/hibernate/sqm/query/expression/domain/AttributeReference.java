/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.query.from.SqmAttributeJoin;

/**
 * Models the binding of a persistent attribute of the domain model.
 *
 * @author Steve Ebersole
 */
public interface AttributeReference extends SqmNavigableReference {
	SqmNavigableReference getLhs();

	AttributeDescriptor getAttribute();

	@Override
	SqmAttributeJoin getFromElement();

	void injectAttributeJoin(SqmAttributeJoin attributeJoin);

}
