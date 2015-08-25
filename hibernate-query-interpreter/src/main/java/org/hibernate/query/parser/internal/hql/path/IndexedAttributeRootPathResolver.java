/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql.path;

import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.expression.IndexedAttributePathPart;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class IndexedAttributeRootPathResolver extends AbstractAttributePathResolverImpl {
	private static final Logger log = Logger.getLogger( IndexedAttributeRootPathResolver.class );

	private final FromElementBuilder fromElementBuilder;
	private final ParsingContext parsingContext;
	private final IndexedAttributePathPart source;

	public IndexedAttributeRootPathResolver(
			FromElementBuilder fromElementBuilder,
			ParsingContext parsingContext,
			IndexedAttributePathPart source) {
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
	public AttributePathPart resolvePath(HqlParser.DotIdentifierSequenceContext path) {
		final String pathText = path.getText();
		log.debugf( "Starting resolution of dot-ident sequence (relative to index-path part [%s]) : %s", source, pathText );

		final String[] parts = pathText.split( "\\." );

//		final String rootPart = parts[0];
//		final AttributeDescriptor initialAttributeReference = source.getTypeDescriptor().getAttributeDescriptor( rootPart );
//		if ( initialAttributeReference == null ) {
//			throw new SemanticException(
//					String.format(
//							Locale.ENGLISH,
//							"Could not resolve path reference [%s] against source type [%s] from indexed collection reference [%s]",
//							rootPart,
//							source.getTypeDescriptor().getTypeName(),
//							source
//					)
//			);
//		}

		final FromElement lhs = resolveAnyIntermediateAttributePathJoins( source.getUnderlyingFromElement(), parts, 0 );
		return new AttributeReferenceExpression( lhs, parts[parts.length-1] );
	}
}
