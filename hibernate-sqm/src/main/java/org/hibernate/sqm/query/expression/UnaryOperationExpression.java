/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class UnaryOperationExpression implements Expression {
	public enum Operation {
		PLUS,
		MINUS
	}

	private final Operation operation;
	private final Expression operand;

	public UnaryOperationExpression(Operation operation,Expression operand) {
		this.operation = operation;
		this.operand = operand;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return getOperand().getTypeDescriptor();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitUnaryOperationExpression( this );
	}

	public Expression getOperand() {
		return operand;
	}

	public Operation getOperation() {
		return operation;
	}
}
