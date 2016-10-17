/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class EntityBinding implements DomainReferenceBinding {
	private final EntityReference entityReference;
	private SqmFrom fromElement;

	public EntityBinding(EntityReference entityReference) {
		this.entityReference = entityReference;
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
	public EntityReference getBoundDomainReference() {
		return entityReference;
	}

	@Override
	public EntityReference getExpressionType() {
		return getBoundDomainReference();
	}

	@Override
	public EntityReference getInferableType() {
		return getBoundDomainReference();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return fromElement.accept( walker );
	}

	@Override
	public String asLoggableText() {
		return entityReference.asLoggableText();
	}
}
