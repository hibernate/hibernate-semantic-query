/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.hql.internal.navigable;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.domain.SqmNavigableSource;
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.query.SqmPropertyPath;
import org.hibernate.sqm.query.expression.domain.AbstractSqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;
import org.hibernate.sqm.domain.SqmExpressableType;
import org.hibernate.sqm.query.from.SqmDowncast;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmFromExporter;

/**
 * Models an "incidental downcast", as opposed to an intrinsic downcast.  An
 * intrinsic downcast occurs in the from-clause - the downcast target becomes
 * an intrinsic part of the FromElement (see {@link SqmFrom#getIntrinsicSubclassIndicator()}.
 * An incidental downcast, on the other hand, occurs outside the from-clause.
 * <p/>
 * For example,
 * {@code .. from Person p where treat(p.address as USAddress).zip=? ...} represents
 * such an intrinsic downcast of Address to one of its subclasses named USAddress.
 *
 * @author Steve Ebersole
 */
public class TreatedNavigableBinding extends AbstractSqmNavigableBinding implements SqmNavigableBinding,
		SqmNavigableSourceBinding {
	private final SqmNavigableBinding baseBinding;
	private final SqmExpressableTypeEntity subclassIndicator;

	public TreatedNavigableBinding(SqmNavigableBinding baseBinding, SqmExpressableTypeEntity subclassIndicator) {
		this.baseBinding = baseBinding;
		this.subclassIndicator = subclassIndicator;

		baseBinding.addDowncast( new SqmDowncast( subclassIndicator ) );
	}

	public SqmExpressableTypeEntity getSubclassIndicator() {
		return subclassIndicator;
	}

	@Override
	public SqmNavigableSource getBoundNavigable() {
		return subclassIndicator;
	}

	@Override
	public SqmNavigableSourceBinding getSourceBinding() {
		return baseBinding.getSourceBinding();
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return subclassIndicator.getExportedDomainType();
	}

	@Override
	public SqmPropertyPath getPropertyPath() {
		return baseBinding.getPropertyPath();
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return subclassIndicator;
	}

	@Override
	public SqmExpressableType getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return baseBinding.accept( walker );
	}

	@Override
	public String asLoggableText() {
		return "TREAT( " + baseBinding.asLoggableText() + " AS " + subclassIndicator.asLoggableText() + " )";
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return ( (SqmFromExporter) baseBinding ).getExportedFromElement();
	}

	@Override
	public void injectExportedFromElement(SqmFrom sqmFrom) {
		( (SqmFromExporter) baseBinding ).injectExportedFromElement( sqmFrom );
	}
}
