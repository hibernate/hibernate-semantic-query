/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import java.util.Locale;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.AttributeBinding;
import org.hibernate.sqm.path.Binding;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmFrom;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class AttributeReferenceSqmExpression implements AttributeBinding, SqmExpression {
	private static final Logger log = Logger.getLogger( AttributeReferenceSqmExpression.class );

	private final Binding source;
	private final Attribute boundAttribute;

	// this refers to the SqmFrom generated for the attribute reference, not its left hand side
	private SqmFrom fromElement;

	public AttributeReferenceSqmExpression(
			Binding source,
			Attribute boundAttribute,
			SqmFrom fromElement) {
		this.source = source;
		this.boundAttribute = boundAttribute;
	}

	@Override
	public Binding getLeftHandSide() {
		return source;
	}

	@Override
	public void injectFromElementGeneratedForAttribute(SqmAttributeJoin join) {
		if ( fromElement != null && fromElement != join ) {
			log.debugf(
					"SqmAttributeJoin [%s] passed to AttributeReferenceSqmExpression.injectFromElementGeneratedForAttribute will overwrite previous [%s]",
					join,
					fromElement
			);
		}

		fromElement = join;
	}

	@Override
	public Attribute getBoundAttribute() {
		return boundAttribute;
	}

	@Override
	public Bindable getBindable() {
		return (Bindable) boundAttribute;
	}

	@Override
	public ManagedType getSubclassIndicator() {
		return null;
	}

	@Override
	public SqmFrom getFromElement() {
		return fromElement;
	}

	@Override
	public String asLoggableText() {
		return source.asLoggableText() + '.' + getBoundAttribute().getName();
	}

	@Override
	public Type getExpressionType() {
		return getBindable().getBoundType();
	}

	@Override
	public Type getInferableType() {
		return getExpressionType();
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ENGLISH,
				"AttributeReferenceExpression{" +
						"path=%s" +
						", attribute-name=%s" +
						", attribute-type=%s" +
						'}',
				asLoggableText(),
				getBoundAttribute().getName(),
				getExpressionType()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitAttributeReferenceExpression( this );
	}

}
