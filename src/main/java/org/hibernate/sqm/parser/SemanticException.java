/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser;

/**
 * Represents an error in the semantics (meaning) of the passed query.  Generally
 * speaking, this is a "user error".
 *
 * @author Steve Ebersole
 */
public class SemanticException extends QueryException {
	public SemanticException(String message) {
		super( message );
	}

	public SemanticException(String message, Throwable cause) {
		super( message, cause );
	}
}
