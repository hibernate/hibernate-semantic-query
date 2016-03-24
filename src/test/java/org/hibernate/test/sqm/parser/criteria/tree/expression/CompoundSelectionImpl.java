/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;

import org.hibernate.sqm.parser.criteria.spi.*;
import org.hibernate.sqm.parser.criteria.spi.expression.CriteriaExpression;
import org.hibernate.sqm.query.select.AliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

/**
 * The Hibernate implementation of the JPA {@link CompoundSelection}
 * contract.
 *
 * @author Steve Ebersole
 */
public class CompoundSelectionImpl<X>
		extends SelectionImpl<X>
		implements CompoundSelection<X>, Serializable {
	private final boolean isConstructor;
	private List<Selection<?>> selectionItems;

	public CompoundSelectionImpl(
			CriteriaBuilderImpl criteriaBuilder,
			Class<X> javaType,
			List<Selection<?>> selectionItems) {
		super( criteriaBuilder, javaType );
		this.isConstructor = !javaType.isArray() && !Tuple.class.isAssignableFrom( javaType );
		this.selectionItems = selectionItems;
	}

	@Override
	public boolean isCompoundSelection() {
		return true;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		return selectionItems;
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, AliasedExpressionContainer container) {
		for ( Selection<?> selectionItem : selectionItems ) {
			container.add(
					( (CriteriaExpression) selectionItem ).visitExpression( visitor ),
					getAlias()
			);
		}
	}
}
