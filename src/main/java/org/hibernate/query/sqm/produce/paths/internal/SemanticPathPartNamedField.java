/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.paths.internal;

import java.lang.reflect.Field;
import java.util.Locale;

import org.hibernate.query.sqm.produce.paths.spi.SemanticPathPart;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathResolutionContext;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;

/**
 * todo (6.0) : this also needs to be a SqmExpression
 *
 * @author Steve Ebersole
 */
public class SemanticPathPartNamedField implements SemanticPathPart {
	private final Field theField;

	public SemanticPathPartNamedField(Field theField) {
		this.theField = theField;
	}

	@Override
	public SemanticPathPart resolvePathPart(
			String name,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		throw new SemanticException(
				String.format(
						Locale.ROOT,
						"A field [%s.%s] cannot be further de-referenced",
						theField.getDeclaringClass().getName(),
						theField.getName()
				)
		);
	}

	@Override
	public SqmNavigableReference resolveIndexedAccess(
			SqmExpression selector,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		throw new SemanticException(
				String.format(
						Locale.ROOT,
						"A field [%s.%s] cannot be further de-referenced",
						theField.getDeclaringClass().getName(),
						theField.getName()
				)
		);
	}
}
