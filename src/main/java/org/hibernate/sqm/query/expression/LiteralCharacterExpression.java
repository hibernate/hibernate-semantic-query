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
public class LiteralCharacterExpression extends AbstractLiteralExpressionImpl<Character> {
	public LiteralCharacterExpression(Character value, BasicType<Character> typeDescriptor) {
		super( value, typeDescriptor );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralCharacterExpression( this );
	}

	@Override
	protected void validateInferredType(Class javaType) {
		if ( !Compatibility.areAssignmentCompatible( javaType, char.class ) ) {
			throw new TypeInferenceException( "Character literal is not convertible to inferred type [" + javaType + "]" );
		}
	}
}
