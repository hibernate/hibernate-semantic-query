/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.hibernate.test.sqm.domain.BasicType;
import org.hibernate.test.sqm.domain.Type;
import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.function.CastFunction;

/**
 * Models an expression in the criteria query language.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractCriteriaExpressionImpl<T>
		extends SelectionImpl<T>
		implements ExpressionImplementor<T>, Serializable {
	private Type sqmType;

	public AbstractCriteriaExpressionImpl(
			CriteriaBuilderImpl criteriaBuilder,
			Type sqmType,
			Class<T> javaType) {
		super( criteriaBuilder, javaType );
	}

	@Override
	public Type getExpressionSqmType() {
		return sqmType;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <X> Expression<X> as(Class<X> type) {
		if ( type.equals( getJavaType() ) ) {
			return (Expression<X>) this;
		}
		else {
			return new CastFunction<>(
					this,
					(BasicType<X>) criteriaBuilder().consumerContext().getDomainMetamodel().resolveBasicType( type ),
					type,
					criteriaBuilder()
			);
		}
	}

	@Override
	public Predicate isNull() {
		return criteriaBuilder().isNull( this );
	}

	@Override
	public Predicate isNotNull() {
		return criteriaBuilder().isNotNull( this );
	}

	@Override
	public Predicate in(Object... values) {
		return criteriaBuilder().in( this, values );
	}

	@Override
	public Predicate in(Expression<?>... values) {
		return criteriaBuilder().in( this, values );
	}

	@Override
	public Predicate in(Collection<?> values) {
		return criteriaBuilder().in( this, values.toArray() );
	}

	@Override
	public Predicate in(Expression<Collection<?>> values) {
		return criteriaBuilder().in( this, values );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public ExpressionImplementor<Long> asLong() {
		resetJavaType( Long.class );
		return (ExpressionImplementor<Long>) this;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public ExpressionImplementor<Integer> asInteger() {
		resetJavaType( Integer.class );
		return (ExpressionImplementor<Integer>) this;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public ExpressionImplementor<Float> asFloat() {
		resetJavaType( Float.class );
		return (ExpressionImplementor<Float>) this;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public ExpressionImplementor<Double> asDouble() {
		resetJavaType( Double.class );
		return (ExpressionImplementor<Double>) this;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public ExpressionImplementor<BigDecimal> asBigDecimal() {
		resetJavaType( BigDecimal.class );
		return (ExpressionImplementor<BigDecimal>) this;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public ExpressionImplementor<BigInteger> asBigInteger() {
		resetJavaType( BigInteger.class );
		return (ExpressionImplementor<BigInteger>) this;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public ExpressionImplementor<String> asString() {
		resetJavaType( String.class );
		return (ExpressionImplementor<String>) this;
	}
}
