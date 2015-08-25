/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser;

/**
 * @author Steve Ebersole
 */
public class StrictJpaComplianceViolation extends SemanticException {
	public static enum Type {
		IMPLICIT_SELECT( "implicit select clause" ),
		ALIASED_FETCH_JOIN( "aliased fetch join" ),
		UNMAPPED_POLYMORPHISM( "unmapped polymorphic reference" )
		;

		private final String description;

		Type(String description) {
			this.description = description;
		}

		private String description() {
			return description;
		}
	}

	private final Type type;

	public StrictJpaComplianceViolation(Type type) {
		super( "Strict JPQL compliance was violated : " + type.description );
		this.type = type;
	}

	public StrictJpaComplianceViolation(String message, Type type) {
		super( message );
		this.type = type;
	}

	public Type getType() {
		return type;
	}
}
