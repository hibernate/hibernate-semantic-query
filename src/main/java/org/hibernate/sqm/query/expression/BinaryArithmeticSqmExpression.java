/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;

/**
 * @author Steve Ebersole
 */
public class BinaryArithmeticSqmExpression implements SqmExpression {
	private final Operation operation;
	private final SqmExpression lhsOperand;
	private final SqmExpression rhsOperand;

	private Navigable expressionType;

	public BinaryArithmeticSqmExpression(
			Operation operation,
			SqmExpression lhsOperand,
			SqmExpression rhsOperand,
			Navigable expressionType) {
		this.operation = operation;
		this.lhsOperand = lhsOperand;
		this.rhsOperand = rhsOperand;
		this.expressionType = expressionType;
	}

	public enum Operation {
		ADD {
			@Override
			String apply(String lhs, String rhs) {
				return applyPrimitive( lhs, '+', rhs );
			}
		},
		SUBTRACT {
			@Override
			String apply(String lhs, String rhs) {
				return applyPrimitive( lhs, '-', rhs );
			}
		},
		MULTIPLY {
			@Override
			String apply(String lhs, String rhs) {
				return applyPrimitive( lhs, '*', rhs );
			}
		},
		DIVIDE {
			@Override
			String apply(String lhs, String rhs) {
				return applyPrimitive( lhs, '/', rhs );
			}
		},
		QUOT {
			@Override
			String apply(String lhs, String rhs) {
				return applyPrimitive( lhs, '/', rhs );
			}
		},
		MODULO {
			@Override
			String apply(String lhs, String rhs) {
//				return lhs + " % " + rhs;
				return "mod(" + lhs + "," + rhs + ")";
			}
		};

		abstract String apply(String lhs, String rhs);

		private static String applyPrimitive(String lhs, char operator, String rhs) {
			return '(' + lhs + operator + rhs + ')';
		}
	}

	/**
	 * Get the left-hand operand.
	 *
	 * @return The left-hand operand.
	 */
	public SqmExpression getLeftHandOperand() {
		return lhsOperand;
	}

	/**
	 * Get the operation
	 *
	 * @return The operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * Get the right-hand operand.
	 *
	 * @return The right-hand operand.
	 */
	public SqmExpression getRightHandOperand() {
		return rhsOperand;
	}

	@Override
	public Navigable getExpressionType() {
		return expressionType;
	}

	@Override
	public Navigable getInferableType() {
		return expressionType;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitBinaryArithmeticExpression( this );
	}

	@Override
	public String asLoggableText() {
		return getOperation().apply( lhsOperand.asLoggableText(), rhsOperand.asLoggableText() );
	}
}
