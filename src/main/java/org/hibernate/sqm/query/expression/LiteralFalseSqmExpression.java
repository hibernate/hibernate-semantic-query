/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;

/**
 * @author Steve Ebersole
 */
public class LiteralFalseSqmExpression extends AbstractLiteralSqmExpressionImpl<Boolean> {
	public LiteralFalseSqmExpression(BasicType<Boolean> booleanTypeDescriptor) {
		super( Boolean.FALSE, booleanTypeDescriptor );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralFalseExpression( this );
	}

	@Override
	protected void validateInferredType(Class javaType) {
		if ( !Compatibility.areAssignmentCompatible( javaType, boolean.class ) ) {
			throw new TypeInferenceException( "Boolean (false) literal is not convertible to inferred type [" + javaType + "]" );
		}
	}
}
