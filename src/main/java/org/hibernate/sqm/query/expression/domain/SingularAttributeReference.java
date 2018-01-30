/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.query.sqm.produce.navigable.spi.NavigableReferenceBuilder;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathPart;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathResolutionContext;
import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.SingularAttributeDescriptor;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.SqmAttributeJoin;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeReference
		extends AbstractAttributeReference<SingularAttributeDescriptor> {
	public SingularAttributeReference(
			SqmNavigableReference lhs,
			SingularAttributeDescriptor attribute) {
		super( lhs, attribute );
	}

	public SingularAttributeReference(
			SqmNavigableReference lhs,
			SingularAttributeDescriptor attribute,
			SqmAttributeJoin join) {
		super( lhs, attribute, join );
	}

	@Override
	public SemanticPathPart resolvePathPart(
			String name,
			String currentContextKey,
			boolean isTerminal,
			SemanticPathResolutionContext context) {
		final AttributeDescriptor attributeDescriptor = context.getParsingContext()
				.getConsumerContext()
				.getDomainMetamodel()
				.resolveAttributeDescriptor( getAttribute(), name );
		return NavigableReferenceBuilder.INSTANCE.buildNavigableReference(
				this,
				attributeDescriptor,
				isTerminal,
				context.getNavigableReferenceBuilderContext()
		);
	}

	@Override
	public SqmNavigableReference resolveIndexedAccess(
			SqmExpression selector,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		throw new SemanticException( "Cannot index-access a singular attribute [" + getAttribute().asLoggableText() );
	}
}
