/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class RelationalSqmPredicate implements SqmPredicate, NegatableSqmPredicate {

	private final SqmExpression leftHandExpression;
	private final SqmExpression rightHandExpression;
	private RelationalPredicateOperator operator;

	public RelationalSqmPredicate(
			RelationalPredicateOperator operator,
			SqmExpression leftHandExpression,
			SqmExpression rightHandExpression) {
		this.leftHandExpression = leftHandExpression;
		this.rightHandExpression = rightHandExpression;
		this.operator = operator;
	}

	public SqmExpression getLeftHandExpression() {
		return leftHandExpression;
	}

	public SqmExpression getRightHandExpression() {
		return rightHandExpression;
	}

	public RelationalPredicateOperator getOperator() {
		return operator;
	}

	@Override
	public boolean isNegated() {
		return false;
	}

	@Override
	public void negate() {
		this.operator = this.operator.negate();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitRelationalPredicate( this );
	}
}
