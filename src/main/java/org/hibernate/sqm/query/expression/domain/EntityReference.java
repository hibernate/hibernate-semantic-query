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
import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.EntityDescriptor;
import org.hibernate.sqm.domain.NoSuchAttributeException;
import org.hibernate.sqm.domain.PluralAttributeDescriptor;
import org.hibernate.sqm.domain.SingularAttributeDescriptor;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class EntityReference implements SqmNavigableReference {
	private final EntityDescriptor entityDescriptor;
	private final PropertyPath propertyPath;

	private SqmFrom fromElement;

	public EntityReference(EntityDescriptor entityDescriptor) {
		this.entityDescriptor = entityDescriptor;
		this.propertyPath = new PropertyPath( null, entityDescriptor.getEntityName() );
	}

	public void injectFromElement(SqmFrom fromElement) {
		assert fromElement.getDomainReferenceBinding() == this;
		this.fromElement = fromElement;
	}

	@Override
	public SqmFrom getFromElement() {
		return fromElement;
	}

	@Override
	public EntityDescriptor getBoundDomainReference() {
		return entityDescriptor;
	}

	@Override
	public PropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public EntityDescriptor getExpressionType() {
		return getBoundDomainReference();
	}

	@Override
	public EntityDescriptor getInferableType() {
		return getBoundDomainReference();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return fromElement.accept( walker );
	}

	@Override
	public String asLoggableText() {
		return entityDescriptor.asLoggableText();
	}

	@Override
	public SemanticPathPart resolvePathPart(
			String name,
			String currentContextKey,
			boolean isTerminal,
			SemanticPathResolutionContext context) {
		try {
			final AttributeDescriptor attributeDescriptor = context.getParsingContext()
					.getConsumerContext()
					.getDomainMetamodel()
					.resolveAttributeDescriptor( entityDescriptor, name );

			// todo (6.0) : might be nice to have a Navigable#createReference(NavigableContainerReference, ??) method

			return NavigableReferenceBuilder.INSTANCE.buildNavigableReference(
					this,
					attributeDescriptor,
					isTerminal,
					context.getNavigableReferenceBuilderContext()
			);
		}
		catch (NoSuchAttributeException e) {
			throw new SemanticException(
					"Could not resolve attribute [" + name +
							"] relative to entity [" + entityDescriptor.getEntityName() + "]"
			);
		}
	}

	@Override
	public SqmNavigableReference resolveIndexedAccess(
			SqmExpression selector,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		throw new SemanticException( "Cannot index-access an entity (" + currentContextKey + ")" );
	}
}
