/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.sqm.ConsumerContext;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.path.AttributeBinding;
import org.hibernate.sqm.query.expression.AttributeReferenceSqmExpression;
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

	private Map<SqmFrom,Map<Attribute,SqmAttributeJoin>> attributeJoinMapByFromElement;
	private List<AttributeReferenceSqmExpression> delayedJoinAttributeReferences;

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
		Map<Attribute,SqmAttributeJoin> attributeJoinMap = null;
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

		final SqmAttributeJoin previous = attributeJoinMap.put( join.getJoinedAttributeDescriptor(), join );
		if ( previous != null ) {
			log.debugf(
					"Caching SqmAttributeJoin [%s] over-wrote previous cache entry [%s]",
					join,
					previous
			);
		}
	}

	public SqmAttributeJoin getCachedAttributeJoin(SqmFrom lhs, Attribute attribute) {
		if ( attributeJoinMapByFromElement == null ) {
			return null;
		}

		final Map<Attribute,SqmAttributeJoin> attributeJoinMap = attributeJoinMapByFromElement.get( lhs );

		if ( attributeJoinMap == null ) {
			return null;
		}

		return attributeJoinMap.get( attribute );
	}

	public void registerAttributeReference(AttributeReferenceSqmExpression reference) {
		if ( reference.getFromElement() != null ) {
			// we only need to track these if they do not already contain their
			// linked from-element
			return;
		}

		if ( delayedJoinAttributeReferences == null ) {
			delayedJoinAttributeReferences = new ArrayList<>();
		}
		delayedJoinAttributeReferences.add( reference );
	}

	public void attributeJoinCreatedNotification(SqmAttributeJoin join) {
		if ( ImplicitAliasGenerator.isImplicitAlias( join.getIdentificationVariable() ) ) {
			// the join was defined in the FROM-clause, so cannot be the "target" of an implicit join
			return;
		}

		if ( delayedJoinAttributeReferences == null ) {
			return;
		}


		final Iterator<AttributeReferenceSqmExpression> references = delayedJoinAttributeReferences.iterator();
		while ( references.hasNext() ) {
			final AttributeBinding reference = references.next();
			if ( reference.getLeftHandSide() == join.getLeftHandSide()
					&& reference.getBoundAttribute() == join.getBoundAttribute() ) {
				reference.injectFromElementGeneratedForAttribute( join );
				references.remove();
			}
		}
	}
}
