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
public class RelationalPredicate implements Predicate {
	public enum Type {
		EQUAL {
			@Override
			public Type negate() {
				return NOT_EQUAL;
			}
		},
		NOT_EQUAL {
			@Override
			public Type negate() {
				return EQUAL;
			}
		},
		GT {
			@Override
			public Type negate() {
				return LE;
			}
		},
		GE {
			@Override
			public Type negate() {
				return LT;
			}
		},
		LT {
			@Override
			public Type negate() {
				return GE;
			}
		},
		LE {
			@Override
			public Type negate() {
				return GT;
			}
		};

		public abstract Type negate();
	}

	private final Expression leftHandExpression;
	private final Expression rightHandExpression;
	private Type type;

	public RelationalPredicate(
			Type type,
			Expression leftHandExpression,
			Expression rightHandExpression) {
		this.leftHandExpression = leftHandExpression;
		this.rightHandExpression = rightHandExpression;
		this.type = type;
	}

	public Expression getLeftHandExpression() {
		return leftHandExpression;
	}

	public Expression getRightHandExpression() {
		return rightHandExpression;
	}

	public Type getType() {
		return type;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitRelationalPredicate( this );
	}
}
