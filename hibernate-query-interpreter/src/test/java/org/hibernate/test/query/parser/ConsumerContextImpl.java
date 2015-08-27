/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser;

import org.hibernate.query.parser.ConsumerContext;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.domain.ModelMetadata;

import org.hibernate.test.sqm.domain.dynamic.DynamicModelMetadata;

/**
 * @author Steve Ebersole
 */
public class ConsumerContextImpl implements ConsumerContext {
	private final ModelMetadata modelMetadata;

	// false (full HQL support) by default
	private boolean strictJpaCompliance;

	public ConsumerContextImpl() {
		this( new DynamicModelMetadata() );
	}

	public ConsumerContextImpl(ModelMetadata modelMetadata) {
		this.modelMetadata = modelMetadata;
	}

	@Override
	public EntityTypeDescriptor resolveEntityReference(String reference) {
		return modelMetadata.resolveEntityReference( reference );
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
