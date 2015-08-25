/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
public class InTupleListPredicate implements InPredicate {
	private final Expression testExpression;
	private final List<Expression> tupleListExpressions;

	public InTupleListPredicate(Expression testExpression) {
		this( testExpression, new ArrayList<Expression>() );
	}

	public InTupleListPredicate(Expression testExpression, Expression... tupleListExpressions) {
		this( testExpression, Helper.toExpandableList( tupleListExpressions ) );
	}

	public InTupleListPredicate(
			Expression testExpression,
			List<Expression> tupleListExpressions) {
		this.testExpression = testExpression;
		this.tupleListExpressions = tupleListExpressions;
	}

	@Override
	public Expression getTestExpression() {
		return testExpression;
	}

	public List<Expression> getTupleListExpressions() {
		return tupleListExpressions;
	}

	public void addExpression(Expression expression) {
		tupleListExpressions.add( expression );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitInTupleListPredicate( this );
	}
}
