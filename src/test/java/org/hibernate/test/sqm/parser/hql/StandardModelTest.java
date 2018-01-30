/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.query.sqm.produce.spi.HqlParseTreeBuilder;
import org.hibernate.query.sqm.produce.spi.SemanticQueryBuilder;
import org.hibernate.sqm.parser.hql.internal.antlr.HqlParser;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;

/**
 * Base class defining a standard domain model
 *
 * @author Steve Ebersole
 */
public abstract class StandardModelTest {
	protected final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

	protected SqmSelectStatement interpretSelect(String query) {
		return (SqmSelectStatement) interpret( query );
	}

	protected SqmStatement interpret(String query) {
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( query );

		final ParsingContext parsingContext = new ParsingContext( consumerContext );
		return SemanticQueryBuilder.buildSemanticModel( parser.statement(), parsingContext );
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl relatedType = metamodel.makeEntityType( "com.acme.Related" );
		relatedType.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		relatedType.makeSingularAttribute(
				"basic2",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		relatedType.makeSingularAttribute(
				"entity",
				relatedType
		);

		EntityTypeImpl somethingType = metamodel.makeEntityType( "com.acme.Something" );
		somethingType.makeSingularAttribute(
				"b",
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		somethingType.makeSingularAttribute(
				"c",
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		somethingType.makeSingularAttribute(
				"basic",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"basic2",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"entity",
				relatedType
		);

		EntityTypeImpl somethingElseType = metamodel.makeEntityType( "com.acme.SomethingElse" );
		somethingElseType.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingElseType.makeSingularAttribute(
				"related1",
				relatedType
		);
		somethingElseType.makeSingularAttribute(
				"related2",
				relatedType
		);

		EntityTypeImpl somethingElse2Type = metamodel.makeEntityType( "com.acme.SomethingElse2" );
		somethingElse2Type.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingElse2Type.makeSingularAttribute(
				"basic2",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);

		return metamodel;
	}
}
