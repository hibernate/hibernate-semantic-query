/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.test;

import org.hibernate.sqm.ConsumerContext;
import org.hibernate.sqm.domain.SqmDomainMetamodel;

/**
 * @author Steve Ebersole
 */
public class ConsumerContextImpl implements ConsumerContext {
	private final SqmDomainMetamodel modelMetadata;

	// false (full HQL support) by default
	private boolean strictJpaCompliance;

	public ConsumerContextImpl(SqmDomainMetamodel modelMetadata) {
		this.modelMetadata = modelMetadata;
	}

	@Override
	public SqmDomainMetamodel getDomainMetamodel() {
		return modelMetadata;
	}

	@Override
	public Class classByName(String name) throws ClassNotFoundException {
		return Class.forName( name );
	}

	@Override
	public boolean useStrictJpaCompliance() {
		return strictJpaCompliance;
	}

	public void enableStrictJpaCompliance() {
		strictJpaCompliance = true;
	}

	public void disableStrictJpaCompliance() {
		strictJpaCompliance = false;
	}
}
