/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.criteria.tree;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.persistence.criteria.Expression;

import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.parser.criteria.tree.select.JpaSelection;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public interface JpaExpression<T> extends Expression<T>, JpaSelection<T> {
	Navigable getExpressionSqmType();

	SqmExpression visitExpression(CriteriaVisitor visitor);

	/**
	 * See {@link javax.persistence.criteria.CriteriaBuilder#toLong}
	 *
	 * @return <tt>this</tt> but as a long
	 */
	JpaExpression<Long> asLong();

	/**
	 * See {@link javax.persistence.criteria.CriteriaBuilder#toInteger}
	 *
	 * @return <tt>this</tt> but as an integer
	 */
	JpaExpression<Integer> asInteger();

	/**
	 * See {@link javax.persistence.criteria.CriteriaBuilder#toFloat}
	 *
	 * @return <tt>this</tt> but as a float
	 */
	JpaExpression<Float> asFloat();

	/**
	 * See {@link javax.persistence.criteria.CriteriaBuilder#toDouble}
	 *
	 * @return <tt>this</tt> but as a double
	 */
	JpaExpression<Double> asDouble();

	/**
	 * See {@link javax.persistence.criteria.CriteriaBuilder#toBigDecimal}
	 *
	 * @return <tt>this</tt> but as a {@link BigDecimal}
	 */
	JpaExpression<BigDecimal> asBigDecimal();

	/**
	 * See {@link javax.persistence.criteria.CriteriaBuilder#toBigInteger}
	 *
	 * @return <tt>this</tt> but as a {@link BigInteger}
	 */
	JpaExpression<BigInteger> asBigInteger();

	/**
	 * See {@link javax.persistence.criteria.CriteriaBuilder#toString}
	 *
	 * @return <tt>this</tt> but as a string
	 */
	JpaExpression<String> asString();
}
