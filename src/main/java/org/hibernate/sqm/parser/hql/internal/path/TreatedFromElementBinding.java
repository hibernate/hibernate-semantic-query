/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal.path;

import java.util.Locale;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.Binding;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * Models an "incidental downcast", as opposed to an intrinsic downcast.  Stated simply
 * an intrinsic downcast occurs in the from-clause; the downcast target becomes an
 * intrinsic part of the FromElement (see {@link SqmFrom#getIntrinsicSubclassIndicator()}.
 * An incidental downcast, on the other hand, occurs outside the from-clause.
 * <p/>
 * For example,
 * {@code .. from Person p where treat(p.address as USAddress).zip=? ...} represents
 * such an intrinsic downcast of Address to one of its subclasses named USAddress.
 *
 * @author Steve Ebersole
 */
public class TreatedFromElementBinding implements Binding {
	private final Binding baseReference;
	private final EntityType subclassIndicator;

	public TreatedFromElementBinding(Binding baseReference, EntityType subclassIndicator) {
		this.baseReference = baseReference;
		this.subclassIndicator = subclassIndicator;
	}

	@Override
	public EntityType getSubclassIndicator() {
		return subclassIndicator;
	}

	@Override
	public SqmFrom getFromElement() {
		return baseReference.getFromElement();
	}

	@Override
	public Bindable getBindable() {
		return subclassIndicator;
	}

	@Override
	public String asLoggableText() {
		return String.format(
				Locale.ROOT,
				"treat(%s as %s)",
				baseReference.asLoggableText(),
				getSubclassIndicator().getName()
		);
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
		return baseReference.accept( walker );
	}
}
