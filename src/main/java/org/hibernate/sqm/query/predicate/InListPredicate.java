/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sqm.Helper;
import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class InListPredicate implements InPredicate {
	private final Expression testExpression;
	private final List<Expression> listExpressions;
	private final boolean negated;

	public InListPredicate(Expression testExpression) {
		this( testExpression, new ArrayList<Expression>() );
	}

	public InListPredicate(Expression testExpression, Expression... listExpressions) {
		this( testExpression, Helper.toExpandableList( listExpressions ) );
	}

	public InListPredicate(
			Expression testExpression,
			List<Expression> listExpressions) {
		this( testExpression, listExpressions, false );
	}

	public InListPredicate(
			Expression testExpression,
			List<Expression> listExpressions,
			boolean negated) {
		this.testExpression = testExpression;
		this.listExpressions = listExpressions;
		this.negated = negated;
	}

	@Override
	public Expression getTestExpression() {
		return testExpression;
	}

	public List<Expression> getListExpressions() {
		return listExpressions;
	}

	public void addExpression(Expression expression) {
		listExpressions.add( expression );
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitInListPredicate( this );
	}
}
