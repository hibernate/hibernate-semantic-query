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
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class InListSqmPredicate extends AbstractNegatableSqmPredicate implements InSqmPredicate {
	private final SqmExpression testExpression;
	private final List<SqmExpression> listExpressions;

	public InListSqmPredicate(SqmExpression testExpression) {
		this( testExpression, new ArrayList<>() );
	}

	public InListSqmPredicate(SqmExpression testExpression, SqmExpression... listExpressions) {
		this( testExpression, Helper.toExpandableList( listExpressions ) );
	}

	public InListSqmPredicate(
			SqmExpression testExpression,
			List<SqmExpression> listExpressions) {
		this( testExpression, listExpressions, false );
	}

	public InListSqmPredicate(
			SqmExpression testExpression,
			List<SqmExpression> listExpressions,
			boolean negated) {
		super( negated );
		this.testExpression = testExpression;
		this.listExpressions = listExpressions;
	}

	@Override
	public SqmExpression getTestExpression() {
		return testExpression;
	}

	public List<SqmExpression> getListExpressions() {
		return listExpressions;
	}

	public void addExpression(SqmExpression expression) {
		listExpressions.add( expression );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitInListPredicate( this );
	}
}
