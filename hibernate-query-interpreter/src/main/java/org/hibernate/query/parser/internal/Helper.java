/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal;

import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.hibernate.query.parser.ParsingException;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static Type toType(Bindable bindable) {
		switch ( bindable.getBindableType() ) {
			case ENTITY_TYPE: {
				return (EntityType) bindable;
			}
			case SINGULAR_ATTRIBUTE: {
				return ( (SingularAttribute) bindable ).getType();
			}
			case PLURAL_ATTRIBUTE: {
				return ( (PluralAttribute) bindable ).getElementType();
			}
			default: {
				throw new ParsingException( "Unexpected Bindable type : " + bindable );
			}
		}
	}

	private Helper() {
	}
}
