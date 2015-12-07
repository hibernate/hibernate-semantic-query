/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser;

/**
 * @author Steve Ebersole
 */
public class LiteralNumberFormatException extends SemanticException {
	public LiteralNumberFormatException(String message) {
		super( message );
	}

	public LiteralNumberFormatException(String message, Throwable cause) {
		super( message, cause );
	}
}
