/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.paths.internal;


import org.hibernate.query.sqm.produce.SqmProductionException;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathPart;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathResolutionContext;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;

/**
 * @author Steve Ebersole
 */
public class SemanticPathPartNamedPackage implements SemanticPathPart {
	private final Package namedPackage;

	public SemanticPathPartNamedPackage(Package namedPackage) {
		this.namedPackage = namedPackage;
	}

	public Package getNamedPackage() {
		return namedPackage;
	}

	@Override
	public SemanticPathPart resolvePathPart(
			String name,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		final String childName = namedPackage.getName() + '.' + name;

		final Package childPackage = Package.getPackage( childName );
		if ( childPackage != null ) {
			return new SemanticPathPartNamedPackage( childPackage );
		}

		// todo (6.0) : it could also be a qualified entity-name which ought to be checked prior to checking as a Class


		// it could also be a Class name within this package
		try {
			final Class namedClass = context.getParsingContext().getConsumerContext().classByName( childName );
			if ( namedClass != null ) {
				return new SemanticPathPartNamedClass( namedClass );
			}
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		throw new SqmProductionException( "Could not resolve path/name : " + childName );
	}

	@Override
	public SqmNavigableReference resolveIndexedAccess(
			SqmExpression selector,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		throw new SemanticException( "Illegal attempt to dereference package name using index-access" );
	}

}
