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
public class LiteralLongExpression extends AbstractLiteralExpressionImpl<Long> {
	public LiteralLongExpression(Long value, BasicType<Long> typeDescriptor) {
		super( value, typeDescriptor );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralLongExpression( this );
	}

	@Override
	protected void validateInferredType(Class javaType) {
		if ( !Compatibility.areAssignmentCompatible( javaType, long.class ) ) {
			throw new TypeInferenceException( "Long literal is not convertible to inferred type [" + javaType + "]" );
		}
	}
}
