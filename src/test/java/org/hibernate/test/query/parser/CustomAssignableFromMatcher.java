/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser;

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
