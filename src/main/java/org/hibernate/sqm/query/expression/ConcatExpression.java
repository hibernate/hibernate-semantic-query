/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.query.Helper;

/**
 * @author Steve Ebersole
 */
public class ConcatExpression implements Expression {
	private final Expression lhsOperand;
	private final Expression rhsOperand;
	private final BasicType resultType;

	public ConcatExpression(Expression lhsOperand, Expression rhsOperand) {
		this( lhsOperand, rhsOperand, (BasicType) lhsOperand.getExpressionType() );
	}

	public ConcatExpression(Expression lhsOperand, Expression rhsOperand, BasicType resultType) {
		this.lhsOperand = lhsOperand;
		this.rhsOperand = rhsOperand;
		this.resultType = resultType;
	}

	public Expression getLeftHandOperand() {
		return lhsOperand;
	}

	public Expression getRightHandOperand() {
		return rhsOperand;
	}

	@Override
	public BasicType getExpressionType() {
		return resultType;
	}

	@Override
	public Type getInferableType() {
		return Helper.firstNonNull( lhsOperand.getInferableType(), rhsOperand.getInferableType() ) ;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitConcatExpression( this );
	}
}
