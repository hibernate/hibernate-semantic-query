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
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.PluralAttributeDescriptor;
import org.hibernate.sqm.domain.SingularAttributeDescriptor;
import org.hibernate.sqm.query.expression.domain.AttributeReference;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;
import org.hibernate.sqm.query.expression.domain.PluralAttributeReference;
import org.hibernate.sqm.query.expression.domain.SingularAttributeReference;
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

	private Map<SqmFrom,Map<AttributeDescriptor,SqmAttributeJoin>> attributeJoinMapByFromElement;

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
		Map<AttributeDescriptor,SqmAttributeJoin> attributeJoinMap = null;
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

	public SqmAttributeJoin getCachedAttributeJoin(SqmFrom lhs, AttributeDescriptor attribute) {
		if ( attributeJoinMapByFromElement == null ) {
			return null;
		}

		final Map<AttributeDescriptor,SqmAttributeJoin> attributeJoinMap = attributeJoinMapByFromElement.get( lhs );

		if ( attributeJoinMap == null ) {
			return null;
		}

		return attributeJoinMap.get( attribute );
	}

	private Map<SqmFrom,Map<AttributeDescriptor,AttributeReference>> attributeBindingsMap;

	public AttributeReference findOrCreateAttributeBinding(
			SqmNavigableReference lhs,
			String attributeName) {
		return findOrCreateAttributeBinding(
				lhs,
				consumerContext.getDomainMetamodel().resolveAttributeDescriptor( lhs.getBoundDomainReference(), attributeName )
		);
	}

	public AttributeReference findOrCreateAttributeBinding(
			SqmNavigableReference lhs,
			AttributeDescriptor attribute) {
		Map<AttributeDescriptor,AttributeReference> bindingsMap = null;

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

		AttributeReference attributeBinding = bindingsMap.get( attribute );
		if ( attributeBinding == null ) {
			if ( attribute instanceof PluralAttributeDescriptor ) {
				attributeBinding = new PluralAttributeReference( lhs, (PluralAttributeDescriptor) attribute );
			}
			else {
				attributeBinding = new SingularAttributeReference( lhs, (SingularAttributeDescriptor) attribute );
			}
			bindingsMap.put( attribute, attributeBinding );
		}

		return attributeBinding;
	}
}
