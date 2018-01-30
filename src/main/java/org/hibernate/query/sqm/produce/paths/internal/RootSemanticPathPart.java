/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.paths.internal;

import org.hibernate.query.sqm.produce.paths.spi.SemanticPathPart;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathResolutionContext;
import org.hibernate.sqm.domain.EntityDescriptor;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.common.FromElementLocator;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;

/**
 * @author Steve Ebersole
 */
public class RootSemanticPathPart implements SemanticPathPart {
	@Override
	public SemanticPathPart resolvePathPart(
			String name,
			String currentContextKey,
			boolean isTerminal,
			SemanticPathResolutionContext context) {
		// At this point we have a "root reference"... the first path part in
		// a potential series of path parts

		final FromElementLocator fromElementLocator = context.getQuerySpecProcessingState();

		// this root reference could be any of:
		// 		1) a from-element alias
		// 		2) an unqualified attribute name exposed from one (and only one!) from-element
		// 		3) an unqualified (imported) entity name
		// 		4) a package name

		// #1
		final SqmNavigableReference aliasedFromElement = fromElementLocator.findFromElementByIdentificationVariable( name );
		if ( aliasedFromElement != null ) {
			return aliasedFromElement;
		}

		// #2
		final SqmNavigableReference unqualifiedAttributeOwner = fromElementLocator.findFromElementExposingAttribute( name );
		if ( unqualifiedAttributeOwner != null ) {
			return unqualifiedAttributeOwner.resolvePathPart( name, currentContextKey, false, context );
		}

		// #3
		final EntityDescriptor entityByName = context.getParsingContext()
				.getConsumerContext()
				.getDomainMetamodel()
				.resolveEntityDescriptor( name );
		if ( entityByName != null ) {
			return new SemanticPathPartNamedEntity( entityByName );
		}

		// #4
		final Package namedPackageRoot = Package.getPackage( name );
		if ( namedPackageRoot != null ) {
			return new SemanticPathPartNamedPackage( namedPackageRoot );
		}

		throw new SemanticException( "Could not resolve path root : " + name );
	}

	@Override
	public SqmNavigableReference resolveIndexedAccess(
			SqmExpression selector,
			String currentContextKey,
			boolean isTerminal,
			SemanticPathResolutionContext context) {
		throw new SemanticException( "Path cannot start with index-access" );
	}
}
