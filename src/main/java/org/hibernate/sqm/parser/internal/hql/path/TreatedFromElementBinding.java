/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.path;

import java.util.Locale;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.FromElementBinding;
import org.hibernate.sqm.query.from.FromElement;

/**
 * Models an "incidental downcast", as opposed to an intrinsic downcast.  Stated simply
 * an intrinsic downcast occurs in the from-clause; the downcast target becomes an
 * intrinsic part of the FromElement (see {@link FromElement#getIntrinsicSubclassIndicator()}.
 * An incidental downcast, on the other hand, occurs outside the from-clause.
 * <p/>
 * For example,
 * {@code .. from Person p where treat(p.address as USAddress).zip=? ...} represents
 * such an intrinsic downcast of Address to one of its subclasses named USAddress.
 *
 * @author Steve Ebersole
 */
public class TreatedFromElementBinding implements FromElementBinding {
	private final FromElement baseReference;
	private final EntityType subclassIndicator;

	public TreatedFromElementBinding(FromElement baseReference, EntityType subclassIndicator) {
		this.baseReference = baseReference;
		this.subclassIndicator = subclassIndicator;
	}

	@Override
	public FromElement getFromElement() {
		return baseReference;
	}

	@Override
	public ManagedType getAttributeContributingType() {
		return getSubclassIndicator();
	}

	@Override
	public EntityType getSubclassIndicator() {
		return subclassIndicator;
	}

	@Override
	public Bindable getBoundModelType() {
		return getFromElement().getBoundModelType();
	}

	@Override
	public String asLoggableText() {
		return String.format(
				Locale.ROOT,
				"treat(%s as %s)",
				getFromElement().asLoggableText(),
				getSubclassIndicator().getName()
		);
	}

	@Override
	public FromElementBinding getBoundFromElementBinding() {
		return this;
	}

	@Override
	public Type getExpressionType() {
		return getSubclassIndicator();
	}

	@Override
	public Type getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return getFromElement().accept( walker );
	}
}
