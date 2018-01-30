/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.query.sqm.produce.paths.spi.SemanticPathPart;
import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public interface SqmNavigableReference extends SqmExpression, SemanticPathPart {
	SqmFrom getFromElement();
	Navigable getBoundDomainReference();

	PropertyPath getPropertyPath();
}
