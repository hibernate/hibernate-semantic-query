/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.parser.common.DomainReferenceBinding;

import org.jboss.logging.Logger;

/**
 * Convenience base class for FromElement implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractFrom implements SqmFrom {
	private static final Logger log = Logger.getLogger( AbstractFrom.class );

	private final FromElementSpace fromElementSpace;
	private final String uid;
	private final String alias;
	private final DomainReferenceBinding binding;
	private final EntityReference subclassIndicator;
	private final String sourcePath;

	private Map<EntityReference,Downcast> downcastMap;

	protected AbstractFrom(
			FromElementSpace fromElementSpace,
			String uid,
			String alias,
			DomainReferenceBinding binding,
			EntityReference subclassIndicator,
			String sourcePath) {
		this.fromElementSpace = fromElementSpace;
		this.uid = uid;
		this.alias = alias;
		this.binding = binding;
		this.subclassIndicator = subclassIndicator;
		this.sourcePath = sourcePath;
	}

	@Override
	public FromElementSpace getContainingSpace() {
		return fromElementSpace;
	}

	@Override
	public String getUniqueIdentifier() {
		return uid;
	}

	@Override
	public String getIdentificationVariable() {
		return alias;
	}

	@Override
	public DomainReferenceBinding getDomainReferenceBinding() {
		return binding;
	}

	@Override
	public DomainReference getExpressionType() {
		return binding.getBoundDomainReference();
	}

	@Override
	public DomainReference getInferableType() {
		return getExpressionType();
	}

	@Override
	public EntityReference getIntrinsicSubclassIndicator() {
		return subclassIndicator;
	}

	@Override
	public String asLoggableText() {
		return sourcePath;
	}

	@Override
	public void addDowncast(Downcast downcast) {
		Downcast existing = null;
		if ( downcastMap == null ) {
			downcastMap = new HashMap<>();
		}
		else {
			existing = downcastMap.get( downcast.getTargetType() );
		}

		final Downcast toPut;
		if ( existing == null ) {
			toPut = downcast;
		}
		else {
			// todo : depending on how we ultimately define TreatedAsInformation defines what exactly needs to happen here..
			//		for now, just keep the existing...
			toPut = existing;
		}

		downcastMap.put( downcast.getTargetType(), toPut );
	}

	@Override
	public Collection<Downcast> getDowncasts() {
		if ( downcastMap == null ) {
			return Collections.emptySet();
		}
		else {
			return downcastMap.values();
		}
	}
}
