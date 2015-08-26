/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal;

/**
 * Handles generating implicit (or synthetic) aliases.
 *
 * @author Steve Ebersole
 */
public class ImplicitAliasGenerator {
	private int unaliasedCount = 0;

	/**
	 * Builds a unique implicit alias.
	 *
	 * @return The generated alias.
	 */
	public synchronized String buildUniqueImplicitAlias() {
		return "<gen:" + unaliasedCount++ + ">";
	}

	/**
	 * Determine if the given alias is implicit.
	 *
	 * @param alias The alias to check
	 * @return True/false.
	 */
	public static boolean isImplicitAlias(String alias) {
		return alias == null || ( alias.startsWith( "<gen:" ) && alias.endsWith( ">" ) );
	}
}
