/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public interface DomainReferenceBinding extends SqmExpression {
	SqmFrom getFromElement();
	DomainReference getBoundDomainReference();

	String asLoggableText();
}
