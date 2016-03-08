/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class RelationalPredicate implements Predicate, NegatablePredicate {
	public enum Operator {
		EQUAL {
			@Override
			public Operator negate() {
				return NOT_EQUAL;
			}
		},
		NOT_EQUAL {
			@Override
			public Operator negate() {
				return EQUAL;
			}
		},
		GT {
			@Override
			public Operator negate() {
				return LE;
			}
		},
		GE {
			@Override
			public Operator negate() {
				return LT;
			}
		},
		LT {
			@Override
			public Operator negate() {
				return GE;
			}
		},
		LE {
			@Override
			public Operator negate() {
				return GT;
			}
		};

		public abstract Operator negate();
	}

	private final Expression leftHandExpression;
	private final Expression rightHandExpression;
	private Operator operator;

	public RelationalPredicate(
			Operator operator,
			Expression leftHandExpression,
			Expression rightHandExpression) {
		this.leftHandExpression = leftHandExpression;
		this.rightHandExpression = rightHandExpression;
		this.operator = operator;
	}

	public Expression getLeftHandExpression() {
		return leftHandExpression;
	}

	public Expression getRightHandExpression() {
		return rightHandExpression;
	}

	public Operator getOperator() {
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
