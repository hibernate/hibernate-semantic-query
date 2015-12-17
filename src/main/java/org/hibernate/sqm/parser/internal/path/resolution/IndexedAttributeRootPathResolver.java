/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.path.resolution;

import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.parser.NotYetImplementedException;
import org.hibernate.sqm.parser.internal.hql.antlr.HqlParser;
import org.hibernate.sqm.parser.internal.FromElementBuilder;
import org.hibernate.sqm.parser.internal.ParsingContext;
import org.hibernate.sqm.path.Binding;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.expression.PluralAttributeIndexedReference;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class IndexedAttributeRootPathResolver extends AbstractPathResolverImpl {
	private static final Logger log = Logger.getLogger( IndexedAttributeRootPathResolver.class );

	private final FromElementBuilder fromElementBuilder;
	private final ParsingContext parsingContext;
	private final PluralAttributeIndexedReference source;

	public IndexedAttributeRootPathResolver(
			FromElementBuilder fromElementBuilder,
			ParsingContext parsingContext,
			PluralAttributeIndexedReference source) {
		this.fromElementBuilder = fromElementBuilder;
		this.parsingContext = parsingContext;
		this.source = source;
	}

	@Override
	protected FromElementBuilder fromElementBuilder() {
		return fromElementBuilder;
	}

	@Override
	protected ParsingContext parsingContext() {
		return parsingContext;
	}
	@Override
	public Binding resolvePath(HqlParser.DotIdentifierSequenceContext path) {
		return resolvePath( path, null );
	}

	@Override
	public Binding resolvePath(
			HqlParser.DotIdentifierSequenceContext path,
			EntityType subclassIndicator) {
		final String pathText = path.getText();
		log.debugf( "Starting resolution of dot-ident sequence (relative to index-path part [%s]) : %s", source, pathText );

		final String[] parts = pathText.split( "\\." );

////		final String rootPart = parts[0];
////		final AttributeDescriptor initialAttributeReference = source.getExpressionType().getBoundAttribute( rootPart );
////		if ( initialAttributeReference == null ) {
////			throw new SemanticException(
////					String.format(
////							Locale.ENGLISH,
////							"Could not resolve path reference [%s] against source type [%s] from indexed collection reference [%s]",
////							rootPart,
////							source.getExpressionType().getTypeName(),
////							source
////					)
////			);
////		}
//
//		final FromElement lhs = resolveAnyIntermediateAttributePathJoins( source.getAttributeBinding()., parts, 0 );
//		return makeAttributeReferenceExpression( lhs, parts[parts.length-1] );
		throw new NotYetImplementedException( "support for de-reference of plural attribute index selection not yet implemented" );
	}
}
