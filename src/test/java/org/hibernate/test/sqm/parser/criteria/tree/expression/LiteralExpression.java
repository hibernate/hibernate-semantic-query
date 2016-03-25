/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression;

import java.io.Serializable;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.spi.expression.LiteralCriteriaExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.select.AliasedSqmExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

/**
 * Represents a literal expression.
 *
 * @author Steve Ebersole
 */
public class LiteralExpression<T> extends AbstractCriteriaExpressionImpl<T>
		implements LiteralCriteriaExpression<T>, Serializable {
	private Object literal;

	@SuppressWarnings({ "unchecked" })
	public LiteralExpression(CriteriaBuilderImpl criteriaBuilder, T literal) {
		this( criteriaBuilder, (Class<T>) determineClass( literal ), literal );
	}

	private static Class determineClass(Object literal) {
		return literal == null ? null : literal.getClass();
	}

	public LiteralExpression(CriteriaBuilderImpl criteriaBuilder, Class<T> javaType, T literal) {
		this( criteriaBuilder, criteriaBuilder.consumerContext().getDomainMetamodel().getBasicType( javaType ), javaType, literal );
		this.literal = literal;
	}

	public LiteralExpression(
			CriteriaBuilderImpl criteriaBuilder,
			BasicType<T> sqmType,
			Class<T> javaType,
			T literal) {
		super( criteriaBuilder, sqmType, javaType );
		assert sqmType != null;
		this.literal = literal;
	}

	@SuppressWarnings({ "unchecked" })
	public T getLiteral() {
		return (T) literal;
	}

	@Override
	public SqmExpression visitExpression(CriteriaVisitor visitor) {
		return visitor.visitLiteral( this );
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, AliasedSqmExpressionContainer container) {
		container.add( visitExpression( visitor ), getAlias() );
	}
}
