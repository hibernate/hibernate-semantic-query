/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.sqm.query.Helper;

/**
 * @author Steve Ebersole
 */
public class ConcatSqmExpression implements SqmExpression {
	private final SqmExpression lhsOperand;
	private final SqmExpression rhsOperand;

	private final SqmDomainTypeBasic resultType;

	public ConcatSqmExpression(SqmExpression lhsOperand, SqmExpression rhsOperand) {
		this( lhsOperand, rhsOperand, (SqmDomainTypeBasic) lhsOperand.getExpressionType() );
	}

	public ConcatSqmExpression(SqmExpression lhsOperand, SqmExpression rhsOperand, SqmDomainTypeBasic resultType) {
		this.lhsOperand = lhsOperand;
		this.rhsOperand = rhsOperand;
		this.resultType = resultType;
	}

	public SqmExpression getLeftHandOperand() {
		return lhsOperand;
	}

	public SqmExpression getRightHandOperand() {
		return rhsOperand;
	}

	@Override
	public SqmDomainTypeBasic getExportedDomainType() {
		return resultType;
	}

	@Override
	public SqmDomainTypeBasic getExpressionType() {
		return getExportedDomainType();
	}

	@Override
	public SqmDomainTypeBasic getInferableType() {
		return (SqmDomainTypeBasic) Helper.firstNonNull( lhsOperand.getInferableType(), rhsOperand.getInferableType() ) ;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitConcatExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "<concat>";
	}
}
