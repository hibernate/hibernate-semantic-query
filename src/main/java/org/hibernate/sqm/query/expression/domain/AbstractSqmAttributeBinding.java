/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmAttribute;
import org.hibernate.sqm.domain.SqmExpressableType;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmFromExporter;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmAttributeBinding<A extends SqmAttribute>
		extends AbstractNavigableBinding
		implements SqmAttributeBinding, SqmFromExporter {
	private final SqmNavigableSourceBinding sourceBinding;
	private final A attribute;
	private final PropertyPath propertyPath;

	private SqmAttributeJoin join;

	public AbstractSqmAttributeBinding(SqmNavigableSourceBinding sourceBinding, A attribute) {
		if ( sourceBinding == null ) {
			throw new IllegalArgumentException( "Source for AttributeBinding cannot be null" );
		}
		if ( attribute == null ) {
			throw new IllegalArgumentException( "Attribute for AttributeBinding cannot be null" );
		}

		this.sourceBinding = sourceBinding;
		this.attribute = attribute;

		this.propertyPath = sourceBinding.getPropertyPath().append( attribute.getAttributeName() );
	}

	@SuppressWarnings("unchecked")
	public AbstractSqmAttributeBinding(SqmAttributeJoin join) {
		this(
				join.getBinding().getSourceBinding(),
				(A) join.getAttributeBinding().getBoundNavigable()
		);
		injectExportedFromElement( join );
	}

	@Override
	public void injectExportedFromElement(SqmFrom attributeJoin) {
		if ( this.join != null && this.join != attributeJoin ) {
			throw new IllegalArgumentException( "Attempting to create multiple SqmFrom references for a single AttributeBinding" );
		}
		this.join = (SqmAttributeJoin) attributeJoin;
	}

	@Override
	public SqmNavigableSourceBinding getSourceBinding() {
		// attribute binding must have a source
		return sourceBinding;
	}

	@Override
	public A getBoundNavigable() {
		return attribute;
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return join;
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return getBoundNavigable();
	}

	@Override
	public SqmExpressableType getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitAttributeReferenceExpression( this );
	}

	@Override
	public PropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public String asLoggableText() {
		if ( join == null || join.getIdentificationVariable() == null ) {
			return getClass().getSimpleName() + '(' + sourceBinding.asLoggableText() + '.' + attribute.getAttributeName() + ")";
		}
		else {
			return getClass().getSimpleName() + '(' + sourceBinding.asLoggableText() + '.' + attribute.getAttributeName() + " : " + join.getIdentificationVariable() + ")";
		}
	}
}
