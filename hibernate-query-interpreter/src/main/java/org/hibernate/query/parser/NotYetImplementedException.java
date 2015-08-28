/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser;

/**
 * @author Steve Ebersole
 */
public class NotYetImplementedException extends RuntimeException {
	public NotYetImplementedException(String message) {
		super( message );
	}

	public NotYetImplementedException() {
		super( "Not yet implemented" );
	}
}
