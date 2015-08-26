/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
