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
		GREATER_THAN {
			@Override
			public Operator negate() {
				return LESS_THAN_OR_EQUAL;
			}
		},
		GREATER_THAN_OR_EQUAL {
			@Override
			public Operator negate() {
				return LESS_THAN;
			}
		},
		LESS_THAN {
			@Override
			public Operator negate() {
				return GREATER_THAN_OR_EQUAL;
			}
		},
		LESS_THAN_OR_EQUAL {
			@Override
			public Operator negate() {
				return GREATER_THAN;
			}
		};

		public abstract Operator negate();
	}

	private final SqmExpression leftHandExpression;
	private final SqmExpression rightHandExpression;
	private Operator operator;

	public RelationalSqmPredicate(
			Operator operator,
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
