/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import javax.persistence.metamodel.Type;

/**
 * Indicates a problem during {@link ImpliedTypeExpression#impliedType(Type)} execution
 *
 * @author Steve Ebersole
 */
public class TypeInferenceException extends RuntimeException {
	public TypeInferenceException(String message) {
		super( message );
	}

	public TypeInferenceException(String message, Throwable cause) {
		super( message, cause );
	}
}
