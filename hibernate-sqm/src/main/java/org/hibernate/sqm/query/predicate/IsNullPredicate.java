/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class IsNullPredicate implements Predicate {
	private final Expression expression;

	private boolean negated;

	public IsNullPredicate(Expression expression) {
		this( expression, false );
	}

	public IsNullPredicate(Expression expression, boolean negated) {
		this.expression = expression;
		this.negated = negated;
	}

	public Expression getExpression() {
		return expression;
	}

	public boolean isNegated() {
		return negated;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitIsNullPredicate( this );
	}
}
