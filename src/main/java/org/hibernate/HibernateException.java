/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate;

/**
 * @author Steve Ebersole
 */
public class HibernateException extends RuntimeException {
	public HibernateException(String message) {
		super( message );
	}

	public HibernateException(String message, Throwable cause) {
		super( message, cause );
	}
}
