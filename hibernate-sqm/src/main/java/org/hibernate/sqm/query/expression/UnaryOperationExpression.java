/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
