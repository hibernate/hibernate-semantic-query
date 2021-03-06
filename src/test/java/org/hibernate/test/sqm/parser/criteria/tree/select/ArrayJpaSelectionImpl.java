/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.parser.criteria.tree.select;

import java.util.List;

import org.hibernate.query.sqm.produce.spi.criteria.CriteriaVisitor;
import org.hibernate.query.sqm.produce.spi.criteria.JpaExpression;
import org.hibernate.query.sqm.tree.select.SqmAliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderException;
import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

/**
 * @author Steve Ebersole
 */
public class ArrayJpaSelectionImpl<T> extends AbstractCompoundSelection<T> {
	public ArrayJpaSelectionImpl(
			CriteriaBuilderImpl criteriaBuilder,
			Class<T> javaType,
			List<JpaExpression<?>> expressions) {
		super( criteriaBuilder, javaType, expressions );
		if ( !javaType.isArray() ) {
			throw new CriteriaBuilderException(
					"Expecting array type as result-type for JpaArraySelection, but found : " +
							javaType.getName()
			);
		}
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, SqmAliasedExpressionContainer container) {
		for ( JpaExpression<?> jpaExpression : getExpressions() ) {
			container.add(
					jpaExpression.visitExpression( visitor ),
					jpaExpression.getAlias()
			);
		}
	}
}
