/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser;

/**
 * Represents a general uncaught problem performing the interpretation.  This might indicate
 * a semantic (user sqm) problem or a bug in the parser.
 *
 * @author Steve Ebersole
 */
public class InterpretationException extends RuntimeException {
	public InterpretationException(String query, Throwable cause) {
		super(
				"Error interpreting query [" + query + "]; this may indicate a semantic (user query) problem or a bug in the parser",
				cause
		);
	}
}
