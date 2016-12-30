/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression;

import java.io.Serializable;
import javax.persistence.criteria.ParameterExpression;

import org.hibernate.sqm.parser.criteria.tree.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.tree.JpaExpression;
import org.hibernate.sqm.query.expression.SqmExpression;

import org.hibernate.test.sqm.domain.Type;
import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

/**
 * Defines a parameter specification, or the information about a parameter (where it occurs, what is
 * its type, etc).
 *
 * @author Steve Ebersole
 */
public class ParameterExpressionImpl<T>
		extends AbstractJpaExpressionImpl<T>
		implements JpaExpression<T>, ParameterExpression<T>, Serializable {
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

	public String getName() {
		return name;
	}

	public Integer getPosition() {
		return position;
	}

	public Class<T> getParameterType() {
		return getJavaType();
	}

	@Override
	public SqmExpression visitExpression(CriteriaVisitor visitor) {
		return visitor.visitParameter( getName(), getPosition(), getJavaType() );
	}
}
