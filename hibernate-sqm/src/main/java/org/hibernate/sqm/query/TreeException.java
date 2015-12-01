/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

/**
 * Indicates a problem constructing a tree or a node within the tree.
 *
 * @author Steve Ebersole
 */
public class TreeException extends RuntimeException {
	public TreeException(String message) {
		super( message );
	}

	public TreeException(String message, Throwable cause) {
		super( message, cause );
	}
}
