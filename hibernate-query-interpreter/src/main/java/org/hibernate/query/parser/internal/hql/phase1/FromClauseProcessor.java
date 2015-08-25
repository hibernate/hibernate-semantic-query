/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql.phase1;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.query.parser.StrictJpaComplianceViolation;
import org.hibernate.query.parser.internal.ImplicitAliasGenerator;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.antlr.HqlParserBaseListener;
import org.hibernate.query.parser.ParsingException;
import org.hibernate.query.parser.SemanticException;
import org.hibernate.query.parser.internal.FromClauseIndex;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.AbstractHqlParseTreeVisitor;
import org.hibernate.query.parser.internal.hql.path.AttributePathResolver;
import org.hibernate.query.parser.internal.hql.path.BasicAttributePathResolverImpl;
import org.hibernate.query.parser.internal.hql.path.JoinPredicatePathResolverImpl;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.domain.PolymorphicEntityTypeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;
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
	private final ParsingContext parsingContext;
	private final FromClauseIndex fromClauseIndex;
	private final FromElementBuilder fromElementBuilder;
	private FromClauseStackNode currentFromClauseStackNode;
	private FromElementSpace currentFromElementSpace;

	private Statement.Type statementType;

	// Using HqlParser.QuerySpecContext references directly did not work in my experience, as each walk
	// seems to build new instances.  So here use the context text as key.
	private final Map<String, FromClauseStackNode> fromClauseMap = new HashMap<String, FromClauseStackNode>();
	private final Map<String, FromElement> fromElementMap = new HashMap<String, FromElement>();

	public FromClauseProcessor(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;

		this.fromClauseIndex = new FromClauseIndex();
		this.fromElementBuilder = new FromElementBuilder( parsingContext, fromClauseIndex );
	}

	public Statement.Type getStatementType() {
		return statementType;
	}

	public FromClauseIndex getFromClauseIndex() {
		return fromClauseIndex;
	}

	public FromElementBuilder getFromElementBuilder() {
		return fromElementBuilder;
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
	}

	@Override
	public void enterUpdateStatement(HqlParser.UpdateStatementContext ctx) {
		statementType = Statement.Type.UPDATE;
	}

	@Override
	public void enterDeleteStatement(HqlParser.DeleteStatementContext ctx) {
		statementType = Statement.Type.DELETE;
	}

	@Override
	public void enterQuerySpec(HqlParser.QuerySpecContext ctx) {
		super.enterQuerySpec( ctx );

		if ( currentFromClauseStackNode == null ) {
			currentFromClauseStackNode = new FromClauseStackNode( new FromClause() );
			fromClauseIndex.registerRootFromClauseNode( currentFromClauseStackNode );
		}
		else {
			currentFromClauseStackNode = new FromClauseStackNode( new FromClause(), currentFromClauseStackNode );
		}
	}

	@Override
	public void exitQuerySpec(HqlParser.QuerySpecContext ctx) {
		fromClauseMap.put( ctx.getText(), currentFromClauseStackNode );

		if ( currentFromClauseStackNode == null ) {
			throw new ParsingException( "Mismatch currentFromClause handling" );
		}
		currentFromClauseStackNode = currentFromClauseStackNode.getParentNode();
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
		final EntityTypeDescriptor entityTypeDescriptor = resolveEntityReference(
				ctx.mainEntityPersisterReference().dotIdentifierSequence()
		);

		if ( PolymorphicEntityTypeDescriptor.class.isInstance( entityTypeDescriptor ) ) {
			if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
				throw new StrictJpaComplianceViolation(
						"Encountered unmapped polymorphic reference [" + entityTypeDescriptor.getTypeName()
								+ "], but strict JPQL compliance was requested",
						StrictJpaComplianceViolation.Type.UNMAPPED_POLYMORPHISM
				);
			}

			// todo : disallow in subqueries as well
		}

		final RootEntityFromElement rootEntityFromElement = fromElementBuilder.makeRootEntityFromElement(
				currentFromElementSpace,
				resolveEntityReference( ctx.mainEntityPersisterReference().dotIdentifierSequence() ),
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
		fromElementMap.put( ctx.getText(), rootEntityFromElement );
	}

	private EntityTypeDescriptor resolveEntityReference(HqlParser.DotIdentifierSequenceContext dotIdentifierSequenceContext) {
		final String entityName = dotIdentifierSequenceContext.getText();
		final EntityTypeDescriptor entityTypeDescriptor = parsingContext.getConsumerContext().resolveEntityReference(
				entityName
		);
		if ( entityTypeDescriptor == null ) {
			throw new SemanticException( "Unresolved entity name : " + entityName );
		}
		return entityTypeDescriptor;
	}

	private String interpretAlias(TerminalNode aliasNode) {
		if ( aliasNode == null ) {
			return parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}
		assert aliasNode.getSymbol().getType() == HqlParser.IDENTIFIER;
		return aliasNode.getText();
	}

	@Override
	public void enterCrossJoin(HqlParser.CrossJoinContext ctx) {
		final EntityTypeDescriptor entityTypeDescriptor = resolveEntityReference(
				ctx.mainEntityPersisterReference().dotIdentifierSequence()
		);

		if ( PolymorphicEntityTypeDescriptor.class.isInstance( entityTypeDescriptor ) ) {
			throw new SemanticException(
					"Unmapped polymorphic references are only valid as query root, not in cross join : " +
							entityTypeDescriptor.getTypeName()
			);
		}

		final CrossJoinedFromElement join = fromElementBuilder.makeCrossJoinedFromElement(
				currentFromElementSpace,
				entityTypeDescriptor,
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
		fromElementMap.put( ctx.getText(), join );
	}

	@Override
	public void enterJpaCollectionJoin(HqlParser.JpaCollectionJoinContext ctx) {
		final QualifiedJoinTreeVisitor visitor = new QualifiedJoinTreeVisitor(
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

		final QualifiedJoinTreeVisitor visitor = new QualifiedJoinTreeVisitor(
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
			if ( !ImplicitAliasGenerator.isImplicitAlias( joinedPath.getAlias() ) ) {
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

	private static class QualifiedJoinTreeVisitor extends AbstractHqlParseTreeVisitor {
		private final FromElementBuilder fromElementBuilder;
		private final FromClauseIndex fromClauseIndex;
		private final ParsingContext parsingContext;
		private final FromElementSpace fromElementSpace;
		private final FromClauseStackNode currentFromClauseNode;

		private QualifiedJoinedFromElement currentJoinRhs;

		public QualifiedJoinTreeVisitor(
				FromElementBuilder fromElementBuilder,
				FromClauseIndex fromClauseIndex,
				ParsingContext parsingContext,
				FromElementSpace fromElementSpace,
				FromClauseStackNode fromClauseNode,
				JoinType joinType,
				String alias,
				boolean fetched) {
			super( parsingContext, fromElementBuilder, fromClauseIndex );
			this.fromElementBuilder = fromElementBuilder;
			this.fromClauseIndex = fromClauseIndex;
			this.parsingContext = parsingContext;
			this.fromElementSpace = fromElementSpace;
			this.currentFromClauseNode = fromClauseNode;
			this.attributePathResolverStack.push(
					new JoinAttributePathResolver(
							fromElementBuilder,
							fromClauseIndex,
							currentFromClauseNode,
							parsingContext,
							fromElementSpace,
							joinType,
							alias,
							fetched
					)
			);
		}

		@Override
		public FromClause getCurrentFromClause() {
			return fromElementSpace.getFromClause();
		}

		@Override
		public FromClauseStackNode getCurrentFromClauseNode() {
			return currentFromClauseNode;
		}

		@Override
		public AttributePathResolver getCurrentAttributePathResolver() {
			return attributePathResolverStack.getCurrent();
		}

		public void setCurrentJoinRhs(QualifiedJoinedFromElement currentJoinRhs) {
			this.currentJoinRhs = currentJoinRhs;
		}

		@Override
		public Predicate visitQualifiedJoinPredicate(HqlParser.QualifiedJoinPredicateContext ctx) {
			if ( currentJoinRhs == null ) {
				throw new ParsingException( "Expecting join RHS to be set" );
			}

			attributePathResolverStack.push(
					new JoinPredicatePathResolverImpl(
							fromElementBuilder,
							fromClauseIndex,
							parsingContext,
							getCurrentFromClauseNode(),
							currentJoinRhs
					)
			);
			try {
				return super.visitQualifiedJoinPredicate( ctx );
			}
			finally {
				attributePathResolverStack.pop();
			}
		}
	}

	private static class JoinAttributePathResolver extends BasicAttributePathResolverImpl {
		private final FromElementBuilder fromElementBuilder;
		private final FromElementSpace fromElementSpace;
		private final JoinType joinType;
		private final String alias;
		private final boolean fetched;

		public JoinAttributePathResolver(
				FromElementBuilder fromElementBuilder,
				FromClauseIndex fromClauseIndex,
				FromClauseStackNode currentFromClauseNode,
				ParsingContext parsingContext,
				FromElementSpace fromElementSpace,
				JoinType joinType,
				String alias,
				boolean fetched) {
			super( fromElementBuilder, fromClauseIndex, parsingContext, currentFromClauseNode );
			this.fromElementBuilder = fromElementBuilder;
			this.fromElementSpace = fromElementSpace;
			this.joinType = joinType;
			this.alias = alias;
			this.fetched = fetched;
		}

		@Override
		protected JoinType getIntermediateJoinType() {
			return joinType;
		}

		protected boolean areIntermediateJoinsFetched() {
			return fetched;
		}

		@Override
		protected AttributePathPart resolveTerminalPathPart(FromElement lhs, String terminalName) {
			return fromElementBuilder.buildAttributeJoin(
					fromElementSpace,
					lhs,
					resolveAttributeDescriptor( lhs, terminalName ),
					alias,
					joinType,
					fetched
			);
		}

		protected AttributeDescriptor resolveAttributeDescriptor(FromElement lhs, String attributeName) {
			final AttributeDescriptor attributeDescriptor = lhs.getTypeDescriptor().getAttributeDescriptor(
					attributeName
			);
			if ( attributeDescriptor == null ) {
				throw new SemanticException(
						"Name [" + attributeName + "] is not a valid attribute on from-element [" +
								lhs.getTypeDescriptor().getTypeName() + "]"
				);
			}

			return attributeDescriptor;
		}

		@Override
		protected AttributePathPart resolveFromElementAliasAsTerminal(FromElement aliasedFromElement) {
			return aliasedFromElement;
		}
	}
}
