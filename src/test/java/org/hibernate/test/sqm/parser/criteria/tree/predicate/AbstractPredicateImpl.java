/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import java.io.Serializable;
import java.util.List;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.query.select.AliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.AbstractCriteriaExpressionImpl;

/**
 * Basic template support for {@link Predicate} implementors providing
 * expression handling, negation and conjunction/disjunction handling.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractPredicateImpl
		extends AbstractCriteriaExpressionImpl<Boolean>
		implements PredicateImplementor, Serializable {

	protected AbstractPredicateImpl(CriteriaBuilderImpl criteriaBuilder, BasicType<Boolean> sqmType) {
		super( criteriaBuilder, sqmType, Boolean.class );
	}

	public boolean isNegated() {
		return false;
	}

	public Predicate not() {
		return new NegatedPredicateWrapper( this );
	}


	@Override
	public org.hibernate.sqm.query.expression.Expression visitExpression(CriteriaVisitor visitor) {
		throw new UnsupportedOperationException( "Not expecting call to visitExpression on predicate" );
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, AliasedExpressionContainer container) {
		throw new UnsupportedOperationException( "Predicates cannot be used as select expression" );
	}


	// Selection ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public final boolean isCompoundSelection() {
		// Should always be false for predicates
		return super.isCompoundSelection();
	}

	@Override
	public final List<Selection<?>> getCompoundSelectionItems() {
		// Should never have sub selection items for predicates
		return super.getCompoundSelectionItems();
	}

}
