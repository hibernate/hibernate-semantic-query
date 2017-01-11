/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.domain.SqmPluralAttributeBinding;

/**
 * @author Steve Ebersole
 */
public class EmptinessSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SqmPluralAttributeBinding expression;

	public EmptinessSqmPredicate(SqmPluralAttributeBinding expression) {
		this( expression, false );
	}

	public EmptinessSqmPredicate(SqmPluralAttributeBinding expression, boolean negated) {
		super( negated );
		this.expression = expression;
	}

	public SqmPluralAttributeBinding getExpression() {
		return expression;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitIsEmptyPredicate( this );
	}
}
