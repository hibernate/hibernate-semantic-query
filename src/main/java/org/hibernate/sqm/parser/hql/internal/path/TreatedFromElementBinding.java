/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal.path;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.parser.common.DomainReferenceBinding;
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
public class TreatedFromElementBinding implements DomainReferenceBinding {
	private final DomainReferenceBinding baseBinding;
	private final EntityReference subclassIndicator;

	public TreatedFromElementBinding(DomainReferenceBinding baseBinding, EntityReference subclassIndicator) {
		this.baseBinding = baseBinding;
		this.subclassIndicator = subclassIndicator;
	}

	public EntityReference getSubclassIndicator() {
		return subclassIndicator;
	}

	@Override
	public SqmFrom getFromElement() {
		return baseBinding.getFromElement();
	}

	@Override
	public DomainReference getBoundDomainReference() {
		return baseBinding.getBoundDomainReference();
	}

	@Override
	public DomainReference getExpressionType() {
		return getSubclassIndicator();
	}

	@Override
	public DomainReference getInferableType() {
		return getSubclassIndicator();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return baseBinding.accept( walker );
	}

	@Override
	public String asLoggableText() {
		return "TREAT( " + baseBinding.asLoggableText() + " AS " + subclassIndicator.asLoggableText() + " )";
	}
}
