/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.paths.internal;

import java.lang.reflect.Field;

import org.hibernate.query.sqm.produce.paths.spi.SemanticPathPart;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathResolutionContext;
import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;

/**
 * todo (6.0) : this also needs to be a SqmExpression
 *
 * @author Steve Ebersole
 */
public class SemanticPathPartNamedClass implements SemanticPathPart, SqmExpression {
	private final Class theClass;

	public SemanticPathPartNamedClass(Class theClass) {
		this.theClass = theClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	public SemanticPathPart resolvePathPart(
			String name,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		if ( theClass.isEnum() ) {
			try {
				final Enum enumValue = Enum.valueOf( theClass, name );
				return new SemanticPathPartNamedEnum( enumValue );
			}
			catch (IllegalArgumentException ignore) {
			}
		}

		try {
			final Field declaredField = theClass.getDeclaredField( name );
			return new SemanticPathPartNamedField( declaredField );
		}
		catch (NoSuchFieldException ignore) {
		}

		throw new SemanticException( "Could not resolve path relative to class : " + theClass.getName() + '#' + name );
	}

	@Override
	public SqmNavigableReference resolveIndexedAccess(
			SqmExpression selector,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		throw new SemanticException( "Illegal attempt to dereference package name using index-access" );
	}

	@Override
	public Navigable getExpressionType() {
		// todo (6.0) : this was a mistake that I think (hope) is already fixed on 6.0
		//		an expression need not be a NavigableReference
		return null;
	}

	@Override
	public Navigable getInferableType() {
		// todo (6.0) : this was a mistake that I think (hope) is already fixed on 6.0
		//		an expression need not be a NavigableReference
		return null;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		// todo (6.0) : deal with this
		return null;
	}

	@Override
	public String asLoggableText() {
		return "Class reference : " + theClass.getName();
	}
}
