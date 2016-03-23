/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression;

import java.io.Serializable;

import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.select.AliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.path.AbstractPathImpl;

/**
 * Used to construct the result of {@link javax.persistence.criteria.Path#type()}
 *
 * @author Steve Ebersole
 */
public class PathTypeExpression<T> extends ExpressionImpl<T> implements Serializable {
	private final AbstractPathImpl<T> pathImpl;

	public PathTypeExpression(CriteriaBuilderImpl criteriaBuilder, Class<T> javaType, AbstractPathImpl<T> pathImpl) {
		super( criteriaBuilder, javaType );
		this.pathImpl = pathImpl;
	}

	@Override
	public Expression visitExpression(CriteriaVisitor visitor) {
		throw new NotYetImplementedException(  );
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, AliasedExpressionContainer container) {
		throw new NotYetImplementedException(  );
	}
}
