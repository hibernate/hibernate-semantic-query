/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

/**
 * @author Steve Ebersole
 */
public class AttributeResolutionException extends RuntimeException {
	public AttributeResolutionException(String message) {
		super( message );
	}

	public AttributeResolutionException(String message, Throwable cause) {
		super( message, cause );
	}
}
