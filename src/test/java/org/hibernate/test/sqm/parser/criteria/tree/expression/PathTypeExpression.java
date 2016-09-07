/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression;

import java.io.Serializable;

import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.select.SqmAliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.path.AbstractPathImpl;

/**
 * Used to construct the result of {@link javax.persistence.criteria.Path#type()}
 *
 * @author Steve Ebersole
 */
public class PathTypeExpression<T> extends AbstractCriteriaExpressionImpl<T> implements Serializable {
	private final AbstractPathImpl<T> pathImpl;

	public PathTypeExpression(
			CriteriaBuilderImpl criteriaBuilder,
			Type sqmType,
			Class<T> javaType,
			AbstractPathImpl<T> pathImpl) {
		super( criteriaBuilder, sqmType, javaType );
		this.pathImpl = pathImpl;
	}

	@Override
	public SqmExpression visitExpression(CriteriaVisitor visitor) {
		throw new NotYetImplementedException(  );
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, SqmAliasedExpressionContainer container) {
		throw new NotYetImplementedException(  );
	}
}
