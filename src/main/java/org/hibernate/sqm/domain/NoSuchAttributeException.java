/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

import org.hibernate.sqm.parser.SemanticException;

/**
 * Indicates that an attribute referenced in the query could not be resolved.
 *
 * @see DomainMetamodel#resolveAttributeReference
 *
 * @author Steve Ebersole
 */
public class NoSuchAttributeException extends SemanticException {
	public NoSuchAttributeException(String message) {
		super( message );
	}
}
