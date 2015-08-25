/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser;

/**
 * The root exception for errors (potential bugs) in the query parser code itself, as opposed
 * to {@link QueryException} which indicates problems with the query.
 *
 * @author Steve Ebersole
 */
public class ParsingException extends RuntimeException {
	public ParsingException(String message) {
		super( message );
	}

	public ParsingException(String message, Throwable cause) {
		super( message, cause );
	}
}
