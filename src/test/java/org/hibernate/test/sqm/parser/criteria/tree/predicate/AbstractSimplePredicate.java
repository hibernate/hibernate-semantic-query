/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.parser.criteria.tree.CriteriaVisitor;
import org.hibernate.sqm.query.expression.SqmExpression;

import org.hibernate.test.sqm.domain.BasicType;
import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;


/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public abstract class AbstractSimplePredicate
		extends AbstractPredicateImpl 
		implements Serializable {
	private static final List<Expression<Boolean>> NO_EXPRESSIONS = Collections.emptyList();

	public AbstractSimplePredicate(CriteriaBuilderImpl criteriaBuilder) {
		this(
				criteriaBuilder,
				(BasicType<Boolean>) criteriaBuilder.consumerContext().getDomainMetamodel().resolveBasicType( Boolean.class )
		);
	}

	public AbstractSimplePredicate(CriteriaBuilderImpl criteriaBuilder, BasicType<Boolean> sqmType) {
		super( criteriaBuilder, sqmType );
		assert sqmType != null;
	}


	@Override
	public Predicate.BooleanOperator getOperator() {
		return Predicate.BooleanOperator.AND;
	}

	@Override
	public final List<Expression<Boolean>> getExpressions() {
		return NO_EXPRESSIONS;
	}
}
