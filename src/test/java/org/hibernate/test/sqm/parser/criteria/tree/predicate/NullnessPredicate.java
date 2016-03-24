/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import java.io.Serializable;
import javax.persistence.criteria.Expression;

import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.spi.expression.CriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.predicate.NullnessCriteriaPredicate;
import org.hibernate.sqm.parser.criteria.spi.predicate.UnaryCriteriaPredicate;
import org.hibernate.sqm.query.predicate.Predicate;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.sqm.parser.criteria.spi.expression.UnaryOperatorCriteriaExpression;

/**
 * Defines a {@link javax.persistence.criteria.Predicate} for checking the
 * nullness state of an expression, aka an <tt>IS [NOT] NULL</tt> predicate.
 * <p/>
 * The <tt>NOT NULL</tt> form can be built by calling the constructor and then
 * calling {@link #not}.
 *
 * @author Steve Ebersole
 */
public class NullnessPredicate
		extends AbstractSimplePredicate
		implements NullnessCriteriaPredicate, Serializable {
	private final CriteriaExpression<?> operand;

	/**
	 * Constructs the affirmitive form of nullness checking (<i>IS NULL</i>).  To
	 * construct the negative form (<i>IS NOT NULL</i>) call {@link #not} on the
	 * constructed instance.
	 *
	 * @param criteriaBuilder The query builder from whcih this originates.
	 * @param operand The expression to check.
	 */
	public NullnessPredicate(CriteriaBuilderImpl criteriaBuilder, CriteriaExpression<?> operand) {
		super( criteriaBuilder );
		this.operand = operand;
	}

	@Override
	public CriteriaExpression<?> getOperand() {
		return operand;
	}

	@Override
	public Predicate visitPredicate(CriteriaVisitor visitor) {
		return visitor.visitNullnessPredicate( this );
	}
}
