/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.SubQueryExpression;

/**
 * @author Steve Ebersole
 */
public class InSubQueryPredicate implements InPredicate {
	private final Expression testExpression;
	private final SubQueryExpression subQueryExpression;

	public InSubQueryPredicate(
			Expression testExpression,
			SubQueryExpression subQueryExpression) {
		this.testExpression = testExpression;
		this.subQueryExpression = subQueryExpression;
	}

	@Override
	public Expression getTestExpression() {
		return testExpression;
	}

	public SubQueryExpression getSubQueryExpression() {
		return subQueryExpression;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitInSubQueryPredicate( this );
	}
}
