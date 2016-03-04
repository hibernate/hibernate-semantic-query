/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.path;

/**
 * @author Steve Ebersole
 */
public class PathHelper {
	private PathHelper() {
	}

	public static String[] split(String path) {
		if ( path.startsWith( "." ) ) {
			path = path.substring( 1 );
		}
		return path.split( "\\." );
	}
}
