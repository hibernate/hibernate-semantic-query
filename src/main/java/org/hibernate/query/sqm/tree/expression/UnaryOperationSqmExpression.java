/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.query.sqm.domain.type.SqmDomainType;
import org.hibernate.query.sqm.domain.SqmExpressableType;

/**
 * @author Steve Ebersole
 */
public class UnaryOperationSqmExpression implements ImpliedTypeSqmExpression {
	public enum Operation {
		PLUS,
		MINUS
	}

	private final Operation operation;
	private final SqmExpression operand;

	private SqmDomainTypeBasic typeDescriptor;

	public UnaryOperationSqmExpression(Operation operation, SqmExpression operand) {
		this( operation, operand, (SqmDomainTypeBasic) operand.getExpressionType() );
	}

	public UnaryOperationSqmExpression(Operation operation, SqmExpression operand, SqmDomainTypeBasic typeDescriptor) {
		this.operation = operation;
		this.operand = operand;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public SqmDomainTypeBasic getExpressionType() {
		return typeDescriptor;
	}

	@Override
	public SqmDomainTypeBasic getInferableType() {
		return (SqmDomainTypeBasic) operand.getExpressionType();
	}

	@Override
	public void impliedType(SqmExpressableType type) {
		if ( type != null ) {
			this.typeDescriptor = (SqmDomainTypeBasic) type;
			if ( operand instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) operand ).impliedType( type );
			}
		}
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitUnaryOperationExpression( this );
	}

	@Override
	public String asLoggableText() {
		return ( operation == Operation.MINUS ? '-' : '+' ) + operand.asLoggableText();
	}

	public SqmExpression getOperand() {
		return operand;
	}

	public Operation getOperation() {
		return operation;
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return getExpressionType();
	}
}
