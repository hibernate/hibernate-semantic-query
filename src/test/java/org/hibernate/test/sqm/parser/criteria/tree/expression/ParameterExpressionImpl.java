/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression;

import java.io.Serializable;

import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.spi.expression.ParameterCriteriaExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.select.AliasedSqmExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

/**
 * Defines a parameter specification, or the information about a parameter (where it occurs, what is
 * its type, etc).
 *
 * @author Steve Ebersole
 */
public class ParameterExpressionImpl<T>
		extends AbstractCriteriaExpressionImpl<T>
		implements ParameterCriteriaExpression<T>, Serializable {
	private final String name;
	private final Integer position;

	public ParameterExpressionImpl(
			CriteriaBuilderImpl criteriaBuilder,
			Type sqmType,
			Class<T> javaType,
			String name) {
		super( criteriaBuilder, sqmType, javaType );
		this.name = name;
		this.position = null;
	}

	public ParameterExpressionImpl(
			CriteriaBuilderImpl criteriaBuilder,
			Type sqmType,
			Class<T> javaType,
			Integer position) {
		super( criteriaBuilder, sqmType, javaType );
		this.name = null;
		this.position = position;
	}

	public ParameterExpressionImpl(
			CriteriaBuilderImpl criteriaBuilder,
			Type sqmType,
			Class<T> javaType) {
		super( criteriaBuilder, sqmType, javaType );
		this.name = null;
		this.position = null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Integer getPosition() {
		return position;
	}

	public Class<T> getParameterType() {
		return getJavaType();
	}

	@Override
	public SqmExpression visitExpression(CriteriaVisitor visitor) {
		return visitor.visitParameter( this );
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, AliasedSqmExpressionContainer container) {
		// for now, disallow parameters as selections.  ultimately would need wrapped in cast
		throw new UnsupportedOperationException( "Parameters are not supported as selections" );
	}
}
