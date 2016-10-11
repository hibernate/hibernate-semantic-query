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

import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.query.Helper;

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
	private final Bindable bindableModelDescriptor;
	private final EntityType subclassIndicator;
	private final String sourcePath;

	private final ManagedType expressionType;

	private Map<EntityType,Downcast> downcastMap;

	protected AbstractFrom(
			FromElementSpace fromElementSpace,
			String uid,
			String alias,
			Bindable bindableModelDescriptor,
			EntityType subclassIndicator,
			String sourcePath) {
		this.fromElementSpace = fromElementSpace;
		this.uid = uid;
		this.alias = alias;
		this.bindableModelDescriptor = bindableModelDescriptor;
		this.subclassIndicator = subclassIndicator;
		this.sourcePath = sourcePath;

		this.expressionType = Helper.determineManagedType( bindableModelDescriptor );
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
	public Bindable getBindable() {
		return bindableModelDescriptor;
	}

	@Override
	public EntityType getIntrinsicSubclassIndicator() {
		return subclassIndicator;
	}

	@Override
	public SqmFrom getFromElement() {
		return this;
	}

	@Override
	public String asLoggableText() {
		return sourcePath;
	}

	@Override
	public EntityType getSubclassIndicator() {
		return subclassIndicator;
	}

	@Override
	public Type getExpressionType() {
		return expressionType;
	}

	@Override
	public Type getInferableType() {
		return getExpressionType();
	}

	@Override
	public void addDowncast(Downcast downcast) {
		Downcast existing = null;
		if ( downcastMap == null ) {
			downcastMap = new HashMap<EntityType, Downcast>();
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
