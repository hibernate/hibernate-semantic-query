/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.hibernate.sqm.ConsumerContext;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;
import org.hibernate.sqm.query.from.SqmFrom;

import org.jboss.logging.Logger;

/**
 * Represents contextual information for each parse
 *
 * @author Steve Ebersole
 */
public class ParsingContext {
	private static final Logger log = Logger.getLogger( ParsingContext.class );

	private final ConsumerContext consumerContext;
	private final ImplicitAliasGenerator aliasGenerator = new ImplicitAliasGenerator();
	private final Map<String,SqmFrom> globalFromElementMap = new HashMap<>();

	private Map<SqmNavigableSourceBinding,Map<SqmNavigable,SqmNavigableBinding>> navigableBindingMapBySource;

	public ParsingContext(ConsumerContext consumerContext) {
		this.consumerContext = consumerContext;
	}

	public ConsumerContext getConsumerContext() {
		return consumerContext;
	}

	public ImplicitAliasGenerator getImplicitAliasGenerator() {
		return aliasGenerator;
	}

	private long uidSequence = 0;

	public String makeUniqueIdentifier() {
		return "<uid:" + ++uidSequence + ">";
	}

	public void registerFromElementByUniqueId(SqmFrom fromElement) {
		final SqmFrom old = globalFromElementMap.put( fromElement.getUniqueIdentifier(), fromElement );
		assert old == null;
	}

	public void findElementByUniqueId(String uid) {
		globalFromElementMap.get( uid );
	}

	public void cacheNavigableBinding(SqmNavigableBinding binding) {
		assert binding.getSourceBinding() != null;

		Map<SqmNavigable, SqmNavigableBinding> navigableBindingMap = null;
		if ( navigableBindingMapBySource == null ) {
			navigableBindingMapBySource = new HashMap<>();
		}
		else {
			navigableBindingMap = navigableBindingMapBySource.get( binding.getSourceBinding() );
		}

		if ( navigableBindingMap == null ) {
			navigableBindingMap = new HashMap<>();
			navigableBindingMapBySource.put( binding.getSourceBinding(), navigableBindingMap );
		}

		final SqmNavigableBinding previous = navigableBindingMap.put( binding.getBoundNavigable(), binding );
		if ( previous != null ) {
			log.debugf(
					"Caching NavigableBinding [%s] over-wrote previous cache entry [%s]",
					binding,
					previous
			);
		}
	}

	public SqmNavigableBinding getCachedNavigableBinding(SqmNavigableSourceBinding source, SqmNavigable navigable) {
		if ( navigableBindingMapBySource == null ) {
			return null;
		}

		final Map<SqmNavigable, SqmNavigableBinding> navigableBindingMap = navigableBindingMapBySource.get( source );

		if ( navigableBindingMap == null ) {
			return null;
		}

		return navigableBindingMap.get( navigable );
	}

	public SqmNavigableBinding findOrCreateNavigableBinding(
			SqmNavigableSourceBinding lhs,
			String navigableName) {
		final SqmNavigable sqmNavigable = lhs.getBoundNavigable().findNavigable( navigableName );

		if ( sqmNavigable == null ) {
			throw new ParsingException(
					String.format(
							Locale.ROOT,
							"Could not resolve SqmNavigable for [%s].[%s]",
							lhs.getPropertyPath().getFullPath(),
							navigableName
					)
			);
		}

		return findOrCreateNavigableBinding( lhs, sqmNavigable );
	}

	public SqmNavigableBinding findOrCreateNavigableBinding(
			SqmNavigableSourceBinding lhs,
			SqmNavigable sqmNavigable) {
		Map<SqmNavigable,SqmNavigableBinding> bindingsMap = null;

		if ( navigableBindingMapBySource == null ) {
			navigableBindingMapBySource = new HashMap<>();
		}
		else {
			bindingsMap = navigableBindingMapBySource.get( lhs );
		}

		if ( bindingsMap == null ) {
			bindingsMap = new HashMap<>();
			navigableBindingMapBySource.put( lhs, bindingsMap );
		}

		return bindingsMap.computeIfAbsent(
				sqmNavigable,
				k -> NavigableBindingHelper.createNavigableBinding(
						lhs,
						sqmNavigable
				)
		);
	}
}
