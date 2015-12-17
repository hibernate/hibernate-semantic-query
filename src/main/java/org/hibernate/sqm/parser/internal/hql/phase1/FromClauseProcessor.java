/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.phase1;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.PolymorphicEntityType;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.StrictJpaComplianceViolation;
import org.hibernate.sqm.parser.internal.AliasRegistry;
import org.hibernate.sqm.parser.internal.FromClauseIndex;
import org.hibernate.sqm.parser.internal.FromElementBuilder;
import org.hibernate.sqm.parser.internal.ImplicitAliasGenerator;
import org.hibernate.sqm.parser.internal.ParsingContext;
import org.hibernate.sqm.parser.internal.hql.antlr.HqlParser;
import org.hibernate.sqm.parser.internal.hql.antlr.HqlParserBaseListener;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedJoinedFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.predicate.Predicate;

import org.jboss.logging.Logger;

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The main piece of Phase 1 processing of an HQL/JPQL statement responsible for processing the from clauses
 * present in the query and building some in-flight indexes (symbol tables) to be used later.
 * <p>
 * This is needed because, just like SQL, the from clause defines the namespace for the query.  We need to
 * know that namespace before we can start processing the other clauses which work within that namespace.
 * <p>
 * E.g., in the HQL {@code select p.name from Person p} we cannot effectively process the {@code p.name}
 * reference in the select clause until after we have processed the from clause and can then recognize that
 * {@code p} is a (forward) reference to the alias {@code p} defined in the from clause.
 *
 * @author Steve Ebersole
 */
public class FromClauseProcessor extends HqlParserBaseListener {
	private static final Logger log = Logger.getLogger( FromClauseProcessor.class );

	private final ParsingContext parsingContext;
	private FromClauseIndex fromClauseIndex;
	private FromElementBuilder fromElementBuilder;
	private FromClauseStackNode currentFromClauseStackNode;
	private FromElementSpace currentFromElementSpace;

	private Statement.Type statementType;

	// Using HqlParser.QuerySpecContext references directly did not work in my experience, as each walk
	// seems to build new instances.  So here use the context text as key.
	private final Map<String, FromClauseStackNode> fromClauseMap = new HashMap<String, FromClauseStackNode>();
	private final Map<String, FromElement> fromElementMap = new HashMap<String, FromElement>();
	private final Map<String, FromElementBuilder> fromElementBuilderMap = new HashMap<String, FromElementBuilder>();

	private RootEntityFromElement dmlRoot;

	public FromClauseProcessor(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
		this.fromClauseIndex = new FromClauseIndex();
		this.fromElementBuilder = new FromElementBuilder( parsingContext, new AliasRegistry() );
	}

	public Statement.Type getStatementType() {
		return statementType;
	}

	public RootEntityFromElement getDmlRoot() {
		return dmlRoot;
	}

	public FromClauseIndex getFromClauseIndex() {
		return fromClauseIndex;
	}

	public FromElementBuilder getFromElementBuilder(HqlParser.QuerySpecContext ctx) {
		return fromElementBuilderMap.get( ctx.getText() );
	}

	public FromClauseStackNode findFromClauseForQuerySpec(HqlParser.QuerySpecContext ctx) {
		return fromClauseMap.get( ctx.getText() );
	}

	@Override
	public void enterSelectStatement(HqlParser.SelectStatementContext ctx) {
		statementType = Statement.Type.SELECT;

		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			if ( ctx.querySpec().selectClause() == null ) {
				throw new StrictJpaComplianceViolation(
						"Encountered implicit select-clause, but strict JPQL compliance was requested",
						StrictJpaComplianceViolation.Type.IMPLICIT_SELECT
				);
			}
		}
	}

	@Override
	public void enterInsertStatement(HqlParser.InsertStatementContext ctx) {
		statementType = Statement.Type.INSERT;

		final EntityType entityType = resolveEntityReference( ctx.insertSpec().intoSpec().dotIdentifierSequence() );
		String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		log.debugf(
				"Generated implicit alias [%s] for INSERT target [%s]",
				alias,
				entityType.getName()
		);

		dmlRoot = new RootEntityFromElement( null, parsingContext.makeUniqueIdentifier(), alias, entityType );
		parsingContext.registerFromElementByUniqueId( dmlRoot );
		fromElementBuilder.getAliasRegistry().registerAlias( dmlRoot );
		fromElementMap.put( ctx.getText(), dmlRoot );
	}

	@Override
	public void enterUpdateStatement(HqlParser.UpdateStatementContext ctx) {
		statementType = Statement.Type.UPDATE;

		dmlRoot = visitDmlRootEntityReference( ctx.mainEntityPersisterReference() );
		fromElementMap.put( ctx.getText(), dmlRoot );
	}

	@Override
	public void enterDeleteStatement(HqlParser.DeleteStatementContext ctx) {
		statementType = Statement.Type.DELETE;

		dmlRoot = visitDmlRootEntityReference( ctx.mainEntityPersisterReference() );
		fromElementMap.put( ctx.getText(), dmlRoot );
	}

	protected RootEntityFromElement visitDmlRootEntityReference(HqlParser.MainEntityPersisterReferenceContext rootEntityContext) {
		final EntityType entityType = resolveEntityReference( rootEntityContext.dotIdentifierSequence() );
		String alias = interpretAlias( rootEntityContext.IDENTIFIER() );
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for DML root entity reference [%s]",
					alias,
					entityType.getName()
			);
		}
		final RootEntityFromElement root = new RootEntityFromElement( null, parsingContext.makeUniqueIdentifier(), alias, entityType );
		parsingContext.registerFromElementByUniqueId( root );
		fromElementBuilder.getAliasRegistry().registerAlias( root );
		return root;
	}

	@Override
	public void enterQuerySpec(HqlParser.QuerySpecContext ctx) {
		super.enterQuerySpec( ctx );

		if ( currentFromClauseStackNode == null ) {
			currentFromClauseStackNode = new FromClauseStackNode( new FromClause() );

			fromClauseIndex.registerRootFromClauseStackNode( currentFromClauseStackNode );
			fromElementBuilder = new FromElementBuilder( parsingContext, fromElementBuilder.getAliasRegistry() );
		}
		else {
			currentFromClauseStackNode = new FromClauseStackNode( new FromClause(), currentFromClauseStackNode );
			fromElementBuilder = new FromElementBuilder(
					parsingContext,
					new AliasRegistry( fromElementBuilder.getAliasRegistry() )
			);
		}
	}

	@Override
	public void exitQuerySpec(HqlParser.QuerySpecContext ctx) {
		fromClauseMap.put( ctx.getText(), currentFromClauseStackNode );
		fromElementBuilderMap.put( ctx.getText(), fromElementBuilder );
		if ( currentFromClauseStackNode == null ) {
			throw new ParsingException( "Mismatch currentFromClause handling" );
		}
		currentFromClauseStackNode = currentFromClauseStackNode.getParentNode();
		if ( fromElementBuilder.getAliasRegistry().getParent() != null ) {
			fromElementBuilder = new FromElementBuilder(
					parsingContext,
					fromElementBuilder.getAliasRegistry().getParent()
			);
		}
		else {
			fromElementBuilder = new FromElementBuilder( parsingContext, new AliasRegistry() );
		}
	}

	@Override
	public void enterFromElementSpace(HqlParser.FromElementSpaceContext ctx) {
		currentFromElementSpace = currentFromClauseStackNode.getFromClause().makeFromElementSpace();
	}

	@Override
	public void exitFromElementSpace(HqlParser.FromElementSpaceContext ctx) {
		currentFromElementSpace = null;
	}

	@Override
	public void enterFromElementSpaceRoot(HqlParser.FromElementSpaceRootContext ctx) {
		final EntityType entityType = resolveEntityReference(
				ctx.mainEntityPersisterReference().dotIdentifierSequence()
		);

		if ( PolymorphicEntityType.class.isInstance( entityType ) ) {
			if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
				throw new StrictJpaComplianceViolation(
						"Encountered unmapped polymorphic reference [" + entityType.getName()
								+ "], but strict JPQL compliance was requested",
						StrictJpaComplianceViolation.Type.UNMAPPED_POLYMORPHISM
				);
			}

			// todo : disallow in subqueries as well
		}

		final RootEntityFromElement rootEntityFromElement = fromElementBuilder.makeRootEntityFromElement(
				currentFromElementSpace,
				entityType,
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
		fromElementMap.put( ctx.getText(), rootEntityFromElement );
	}

	private EntityType resolveEntityReference(HqlParser.DotIdentifierSequenceContext dotIdentifierSequenceContext) {
		final String entityName = dotIdentifierSequenceContext.getText();
		final EntityType entityTypeDescriptor = parsingContext.getConsumerContext()
				.getDomainMetamodel()
				.resolveEntityType( entityName );
		if ( entityTypeDescriptor == null ) {
			throw new SemanticException( "Unresolved entity name : " + entityName );
		}
		return entityTypeDescriptor;
	}

	private String interpretAlias(TerminalNode aliasNode) {
		if ( aliasNode == null ) {
			return parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}

		// todo : not sure I like asserts for this kind of thing.  They are generally disable in runtime environments.
		// either the thing is important to check or it isn't.
		assert aliasNode.getSymbol().getType() == HqlParser.IDENTIFIER;

		return aliasNode.getText();
	}

	@Override
	public void enterCrossJoin(HqlParser.CrossJoinContext ctx) {
		final EntityType entityType = resolveEntityReference(
				ctx.mainEntityPersisterReference().dotIdentifierSequence()
		);

		if ( PolymorphicEntityType.class.isInstance( entityType ) ) {
			throw new SemanticException(
					"Unmapped polymorphic references are only valid as query root, not in cross join : " +
							entityType.getName()
			);
		}

		final CrossJoinedFromElement join = fromElementBuilder.makeCrossJoinedFromElement(
				currentFromElementSpace,
				parsingContext.makeUniqueIdentifier(),
				entityType,
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
		fromElementMap.put( ctx.getText(), join );
	}

	@Override
	public void enterJpaCollectionJoin(HqlParser.JpaCollectionJoinContext ctx) {
		final ParseTreeVisitorQualifiedJoinImpl visitor = new ParseTreeVisitorQualifiedJoinImpl(
				fromElementBuilder,
				fromClauseIndex,
				parsingContext,
				currentFromElementSpace,
				currentFromClauseStackNode,
				JoinType.INNER,
				interpretAlias( ctx.IDENTIFIER() ),
				false
		);

		QualifiedJoinedFromElement joinedPath = (QualifiedJoinedFromElement) ctx.path().accept(
				visitor
		);

		if ( joinedPath == null ) {
			throw new ParsingException( "Could not resolve JPA collection join path : " + ctx.getText() );
		}

		fromElementMap.put( ctx.getText(), joinedPath );
	}

	@Override
	public void enterQualifiedJoin(HqlParser.QualifiedJoinContext ctx) {
		final JoinType joinType;
		if ( ctx.outerKeyword() != null ) {
			// for outer joins, only left outer joins are currently supported
			joinType = JoinType.LEFT;
		}
		else {
			joinType = JoinType.INNER;
		}

		final ParseTreeVisitorQualifiedJoinImpl visitor = new ParseTreeVisitorQualifiedJoinImpl(
				fromElementBuilder,
				fromClauseIndex,
				parsingContext,
				currentFromElementSpace,
				currentFromClauseStackNode,
				joinType,
				interpretAlias( ctx.qualifiedJoinRhs().IDENTIFIER() ),
				ctx.fetchKeyword() != null
		);

		QualifiedJoinedFromElement joinedPath = (QualifiedJoinedFromElement) ctx.qualifiedJoinRhs().path().accept(
				visitor
		);

		if ( joinedPath == null ) {
			throw new ParsingException( "Could not resolve join path : " + ctx.qualifiedJoinRhs().getText() );
		}

		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			if ( !ImplicitAliasGenerator.isImplicitAlias( joinedPath.getIdentificationVariable() ) ) {
				if ( QualifiedAttributeJoinFromElement.class.isInstance( joinedPath ) ) {
					if ( QualifiedAttributeJoinFromElement.class.cast( joinedPath ).isFetched() ) {
						throw new StrictJpaComplianceViolation(
								"Encountered aliased fetch join, but strict JPQL compliance was requested",
								StrictJpaComplianceViolation.Type.ALIASED_FETCH_JOIN
						);
					}
				}
			}
		}

		if ( ctx.qualifiedJoinPredicate() != null ) {
			visitor.setCurrentJoinRhs( joinedPath );
			joinedPath.setOnClausePredicate( (Predicate) ctx.qualifiedJoinPredicate().accept( visitor ) );
		}

		fromElementMap.put( ctx.getText(), joinedPath );
	}

}
