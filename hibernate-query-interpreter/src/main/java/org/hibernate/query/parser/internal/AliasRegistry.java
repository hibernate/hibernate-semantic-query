/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.query.parser.AliasCollisionException;

/**
 * @author Andrea Boriero
 */
public class AliasRegistry {
	Set<String> aliases;

	public AliasRegistry() {
		this.aliases = new HashSet<String>();
	}

	public void registerAlias(String alias) {
		if ( aliases.contains( alias ) ) {
			throw new AliasCollisionException( "Alias collision, alias " + alias + " is used in a different clause" );
		}
		aliases.add( alias );
	}
}
