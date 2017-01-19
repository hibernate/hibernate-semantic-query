/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.parser.criteria.tree.select;

import java.util.List;
import java.util.Map;

import org.hibernate.sqm.parser.criteria.tree.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.tree.JpaExpression;
import org.hibernate.sqm.query.select.SqmAliasedExpressionContainer;
import org.hibernate.sqm.query.select.SqmDynamicInstantiation;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

/**
 * @author Steve Ebersole
 */
public class DynamicInstantiationImpl<T> extends AbstractCompoundSelection<T> {
	public DynamicInstantiationImpl(
			CriteriaBuilderImpl criteriaBuilder,
			Class<T> target,
			List<JpaExpression<?>> arguments) {
		super( criteriaBuilder, target, arguments );
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, SqmAliasedExpressionContainer container) {
		final SqmDynamicInstantiation dynamicInstantiation;

		if ( List.class.equals( getJavaType() ) ) {
			dynamicInstantiation = SqmDynamicInstantiation.forListInstantiation();
		}
		else if ( Map.class.equals( getJavaType() ) ) {
			dynamicInstantiation = SqmDynamicInstantiation.forMapInstantiation();
		}
		else {
			dynamicInstantiation = SqmDynamicInstantiation.forClassInstantiation( getJavaType() );
		}

		for ( JpaExpression<?> argument : getExpressions() ) {
			argument.visitSelections( visitor, dynamicInstantiation );
		}

		container.add( dynamicInstantiation, getAlias() );
	}
}
