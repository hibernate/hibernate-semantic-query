/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce;

import org.hibernate.HibernateException;

/**
 * Base of exception hierarchy for exceptions stemming from
 * producing SQM AST trees
 *
 * @author Steve Ebersole
 */
public class SqmProductionException extends HibernateException {
	public SqmProductionException(String message) {
		super( message );
	}

	public SqmProductionException(String message, Throwable cause) {
		super( message, cause );
	}
}
