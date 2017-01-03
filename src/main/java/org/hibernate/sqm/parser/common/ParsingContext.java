/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.sqm.ConsumerContext;
import org.hibernate.sqm.domain.SqmAttributeReference;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.domain.SqmNavigableSource;
import org.hibernate.sqm.domain.PluralSqmAttributeReference;
import org.hibernate.sqm.domain.SingularSqmAttributeReference;
import org.hibernate.sqm.query.expression.domain.AttributeBinding;
import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;
import org.hibernate.sqm.query.expression.domain.PluralAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SingularAttributeBinding;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
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

	private Map<SqmFrom,Map<SqmAttributeReference,SqmAttributeJoin>> attributeJoinMapByFromElement;

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

	public void cacheAttributeJoin(SqmFrom lhs, SqmAttributeJoin join) {
		Map<SqmAttributeReference,SqmAttributeJoin> attributeJoinMap = null;
		if ( attributeJoinMapByFromElement == null ) {
			attributeJoinMapByFromElement = new HashMap<>();
		}
		else {
			attributeJoinMap = attributeJoinMapByFromElement.get( lhs );
		}

		if ( attributeJoinMap == null ) {
			attributeJoinMap = new HashMap<>();
			attributeJoinMapByFromElement.put( lhs, attributeJoinMap );
		}

		final SqmAttributeJoin previous = attributeJoinMap.put( join.getAttributeBinding().getAttribute(), join );
		if ( previous != null ) {
			log.debugf(
					"Caching SqmAttributeJoin [%s] over-wrote previous cache entry [%s]",
					join,
					previous
			);
		}
	}

	public SqmAttributeJoin getCachedAttributeJoin(SqmFrom lhs, SqmAttributeReference attribute) {
		if ( attributeJoinMapByFromElement == null ) {
			return null;
		}

		final Map<SqmAttributeReference,SqmAttributeJoin> attributeJoinMap = attributeJoinMapByFromElement.get( lhs );

		if ( attributeJoinMap == null ) {
			return null;
		}

		return attributeJoinMap.get( attribute );
	}

	private Map<SqmNavigableSource,Map<SqmNavigable,AttributeBinding>> attributeBindingsMap;

	public SqmNavigable findOrCreateNavigable(SqmNavigableSource lhs, String navigableName) {
		Map<SqmAttributeReference,AttributeBinding> bindingsMap = null;

		if ( attributeBindingsMap == null ) {
			attributeBindingsMap = new HashMap<>();
		}
		else {
			bindingsMap = attributeBindingsMap.get( lhs.getFromElement() );
		}

		if ( bindingsMap == null ) {
			bindingsMap = new HashMap<>();
			attributeBindingsMap.put( lhs.getFromElement(), bindingsMap );
		}

		AttributeBinding attributeBinding = bindingsMap.get( attribute );
		if ( attributeBinding == null ) {
			if ( attribute instanceof PluralSqmAttributeReference ) {
				attributeBinding = new PluralAttributeBinding( lhs, (PluralSqmAttributeReference) attribute );
			}
			else {
				attributeBinding = new SingularAttributeBinding( lhs, (SingularSqmAttributeReference) attribute );
			}
			bindingsMap.put( attribute, attributeBinding );
		}

		return findOrCreateNavigable(
				lhs,
				consumerContext.getDomainMetamodel().resolveNavigable( lhs.getBoundDomainReference(), navigableName )
		);
	}

	public SqmNavigable findOrCreateNavigable(
			SqmNavigableSource lhs,
			Na attribute) {
		Map<SqmAttributeReference,AttributeBinding> bindingsMap = null;

		if ( attributeBindingsMap == null ) {
			attributeBindingsMap = new HashMap<>();
		}
		else {
			bindingsMap = attributeBindingsMap.get( lhs.getFromElement() );
		}

		if ( bindingsMap == null ) {
			bindingsMap = new HashMap<>();
			attributeBindingsMap.put( lhs.getFromElement(), bindingsMap );
		}

		AttributeBinding attributeBinding = bindingsMap.get( attribute );
		if ( attributeBinding == null ) {
			if ( attribute instanceof PluralSqmAttributeReference ) {
				attributeBinding = new PluralAttributeBinding( lhs, (PluralSqmAttributeReference) attribute );
			}
			else {
				attributeBinding = new SingularAttributeBinding( lhs, (SingularSqmAttributeReference) attribute );
			}
			bindingsMap.put( attribute, attributeBinding );
		}

		return attributeBinding;
	}
}
