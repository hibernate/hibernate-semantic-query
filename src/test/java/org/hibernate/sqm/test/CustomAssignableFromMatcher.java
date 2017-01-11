/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.test;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;

/**
 * @author Steve Ebersole
 */
public class CustomAssignableFromMatcher extends CustomMatcher<Class> {
	private final Class check;

	public static Matcher<Class> isCastableAs(Class check) {
		return new CustomAssignableFromMatcher( check );
	}

	public CustomAssignableFromMatcher(Class check) {
		super( "<an instance assignable from [" + check + "]>" );
		this.check = check;
	}

	@Override
	public boolean matches(Object item) {
		if ( item == null ) {
			return false;
		}

		if ( item instanceof Class ) {
			return check.isAssignableFrom( (Class) item );
		}
		else {
			return check.isInstance( item );
		}
	}
}
