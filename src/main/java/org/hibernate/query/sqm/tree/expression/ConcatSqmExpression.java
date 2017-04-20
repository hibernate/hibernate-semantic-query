/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.query.sqm.tree.internal.Helper;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeBasic;

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
