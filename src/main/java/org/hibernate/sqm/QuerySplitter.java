/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.PolymorphicEntityType;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.path.AttributeBindingSource;
import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.SqmDeleteStatement;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.query.SqmUpdateStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceSqmExpression;
import org.hibernate.sqm.query.expression.function.AvgFunctionSqmExpression;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
import org.hibernate.sqm.query.expression.ConcatSqmExpression;
import org.hibernate.sqm.query.expression.ConstantEnumSqmExpression;
import org.hibernate.sqm.query.expression.ConstantFieldSqmExpression;
import org.hibernate.sqm.query.expression.function.ConcatFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountStarFunctionSqmExpression;
import org.hibernate.sqm.query.expression.EntityTypeSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.function.GenericFunctionSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigDecimalSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralCharacterSqmExpression;
import org.hibernate.sqm.query.expression.LiteralDoubleSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFalseSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFloatSqmExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralLongSqmExpression;
import org.hibernate.sqm.query.expression.LiteralNullSqmExpression;
import org.hibernate.sqm.query.expression.LiteralStringSqmExpression;
import org.hibernate.sqm.query.expression.LiteralTrueSqmExpression;
import org.hibernate.sqm.query.expression.function.MaxFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MinFunctionSqmExpression;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.PositionalParameterSqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;
import org.hibernate.sqm.query.expression.function.SumFunctionSqmExpression;
import org.hibernate.sqm.query.expression.UnaryOperationSqmExpression;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedEntityJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.internal.SqmSelectStatementImpl;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.BetweenSqmPredicate;
import org.hibernate.sqm.query.predicate.EmptinessSqmPredicate;
import org.hibernate.sqm.query.predicate.GroupedSqmPredicate;
import org.hibernate.sqm.query.predicate.InSubQuerySqmPredicate;
import org.hibernate.sqm.query.predicate.InListSqmPredicate;
import org.hibernate.sqm.query.predicate.LikeSqmPredicate;
import org.hibernate.sqm.query.predicate.MemberOfSqmPredicate;
import org.hibernate.sqm.query.predicate.NegatedSqmPredicate;
import org.hibernate.sqm.query.predicate.NullnessSqmPredicate;
import org.hibernate.sqm.query.predicate.OrSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.select.SqmDynamicInstantiation;
import org.hibernate.sqm.query.select.SqmDynamicInstantiationArgument;
import org.hibernate.sqm.query.select.SqmSelectClause;
import org.hibernate.sqm.query.select.SqmSelection;
import org.hibernate.sqm.query.set.SqmAssignment;
import org.hibernate.sqm.query.set.SqmSetClause;

/**
 * Handles splitting queries containing unmapped polymorphic references.
 *
 * @author Steve Ebersole
 */
public class QuerySplitter {
	public static SqmSelectStatement[] split(SqmSelectStatement statement) {
		// We only allow unmapped polymorphism in a very restricted way.  Specifically,
		// the unmapped polymorphic reference can only be a root and can be the only
		// root.  Use that restriction to locate the unmapped polymorphic reference
		RootEntityFromElement unmappedPolymorphicReference = null;
		for ( FromElementSpace fromElementSpace : statement.getQuerySpec().getFromClause().getFromElementSpaces() ) {
			if ( PolymorphicEntityType.class.isInstance( fromElementSpace.getRoot().getBoundModelType() ) ) {
				unmappedPolymorphicReference = fromElementSpace.getRoot();
			}
		}

		if ( unmappedPolymorphicReference == null ) {
			return new SqmSelectStatement[] { statement };
		}

		final PolymorphicEntityType<?> unmappedPolymorphicDescriptor = (PolymorphicEntityType) unmappedPolymorphicReference.getBoundModelType();
		final SqmSelectStatement[] expanded = new SqmSelectStatement[ unmappedPolymorphicDescriptor.getImplementors().size() ];

		int i = -1;
		for ( EntityType mappedDescriptor : unmappedPolymorphicDescriptor.getImplementors() ) {
			i++;
			final UnmappedPolymorphismReplacer replacer = new UnmappedPolymorphismReplacer(
					statement,
					unmappedPolymorphicReference,
					mappedDescriptor
			);
			expanded[i] = replacer.visitSelectStatement( statement );
		}

		return expanded;
	}

	private static class UnmappedPolymorphismReplacer extends BaseSemanticQueryWalker {
		private final RootEntityFromElement unmappedPolymorphicFromElement;
		private final EntityType mappedDescriptor;

		private Map<FromElement,FromElement> fromElementCopyMap = new HashMap<FromElement, FromElement>();

		private UnmappedPolymorphismReplacer(
				SqmSelectStatement selectStatement,
				RootEntityFromElement unmappedPolymorphicFromElement,
				EntityType mappedDescriptor) {
			this.unmappedPolymorphicFromElement = unmappedPolymorphicFromElement;
			this.mappedDescriptor = mappedDescriptor;
		}

		@Override
		public SqmStatement visitStatement(SqmStatement statement) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SqmUpdateStatement visitUpdateStatement(SqmUpdateStatement statement) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SqmSetClause visitSetClause(SqmSetClause setClause) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SqmAssignment visitAssignment(SqmAssignment assignment) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SqmDeleteStatement visitDeleteStatement(SqmDeleteStatement statement) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SqmSelectStatement visitSelectStatement(SqmSelectStatement statement) {
			final SqmSelectStatementImpl copy = new SqmSelectStatementImpl();
			copy.applyQuerySpec( visitQuerySpec( statement.getQuerySpec() ) );
			copy.applyOrderByClause( visitOrderByClause( statement.getOrderByClause() ) );
			return copy;
		}

		@Override
		public SqmQuerySpec visitQuerySpec(SqmQuerySpec querySpec) {
			// NOTE : it is important that we visit the SqmFromClause first so that the
			// 		fromElementCopyMap gets built before other parts of the queryspec
			// 		are visited
			return new SqmQuerySpec(
					visitFromClause( querySpec.getFromClause() ),
					visitSelectClause( querySpec.getSelectClause() ),
					visitWhereClause( querySpec.getWhereClause() )
			);
		}

		private SqmFromClause currentFromClauseCopy = null;

		@Override
		public SqmFromClause visitFromClause(SqmFromClause fromClause) {
			final SqmFromClause previousCurrent = currentFromClauseCopy;

			try {
				SqmFromClause copy = new SqmFromClause();
				currentFromClauseCopy = copy;
				super.visitFromClause( fromClause );
//				for ( FromElementSpace space : fromClause.getFromElementSpaces() ) {
//					visitFromElementSpace( space );
//				}
				return copy;
			}
			finally {
				currentFromClauseCopy = previousCurrent;
			}
		}

		private FromElementSpace currentFromElementSpaceCopy;

		@Override
		public FromElementSpace visitFromElementSpace(FromElementSpace fromElementSpace) {
			if ( currentFromClauseCopy == null ) {
				throw new ParsingException( "Current SqmFromClause copy was null" );
			}

			final FromElementSpace previousCurrent = currentFromElementSpaceCopy;
			try {
				FromElementSpace copy = currentFromClauseCopy.makeFromElementSpace();
				currentFromElementSpaceCopy = copy;
				super.visitFromElementSpace( fromElementSpace );
				return copy;
			}
			finally {
				currentFromElementSpaceCopy = previousCurrent;
			}
		}

		@Override
		public RootEntityFromElement visitRootEntityFromElement(RootEntityFromElement rootEntityFromElement) {
			final RootEntityFromElement existingCopy = (RootEntityFromElement) fromElementCopyMap.get( rootEntityFromElement );
			if ( existingCopy != null ) {
				return existingCopy;
			}

			if ( currentFromElementSpaceCopy == null ) {
				throw new ParsingException( "Current FromElementSpace copy was null" );
			}
			if ( currentFromElementSpaceCopy.getRoot() != null ) {
				throw new ParsingException( "FromElementSpace copy already contains root." );
			}

			final RootEntityFromElement copy;
			if ( rootEntityFromElement == unmappedPolymorphicFromElement ) {
				copy = new RootEntityFromElement(
						currentFromElementSpaceCopy,
						rootEntityFromElement.getUniqueIdentifier(),
						rootEntityFromElement.getIdentificationVariable(),
						mappedDescriptor
				);
			}
			else {
				copy = new RootEntityFromElement(
						currentFromElementSpaceCopy,
						rootEntityFromElement.getUniqueIdentifier(),
						rootEntityFromElement.getIdentificationVariable(),
						rootEntityFromElement.getBoundModelType()
				);
			}
			fromElementCopyMap.put( rootEntityFromElement, copy );
			return copy;
		}

		@Override
		public Object visitCrossJoinedFromElement(CrossJoinedFromElement joinedFromElement) {
			final CrossJoinedFromElement existingCopy = (CrossJoinedFromElement) fromElementCopyMap.get( joinedFromElement );
			if ( existingCopy != null ) {
				return existingCopy;
			}

			if ( currentFromElementSpaceCopy == null ) {
				throw new ParsingException( "Current FromElementSpace copy was null" );
			}

			CrossJoinedFromElement copy = new CrossJoinedFromElement(
					currentFromElementSpaceCopy,
					joinedFromElement.getUniqueIdentifier(),
					joinedFromElement.getIdentificationVariable(),
					joinedFromElement.getBoundModelType()
			);
			fromElementCopyMap.put( joinedFromElement, copy );
			return copy;
		}

		@Override
		public Object visitQualifiedEntityJoinFromElement(QualifiedEntityJoinFromElement joinedFromElement) {
			final QualifiedEntityJoinFromElement existingCopy = (QualifiedEntityJoinFromElement) fromElementCopyMap.get( joinedFromElement );
			if ( existingCopy != null ) {
				return existingCopy;
			}

			if ( currentFromElementSpaceCopy == null ) {
				throw new ParsingException( "Current FromElementSpace copy was null" );
			}

			QualifiedEntityJoinFromElement copy = new QualifiedEntityJoinFromElement(
					currentFromElementSpaceCopy,
					joinedFromElement.getUniqueIdentifier(),
					joinedFromElement.getIdentificationVariable(),
					joinedFromElement.getBoundModelType(),
					joinedFromElement.getJoinType()
			);
			fromElementCopyMap.put( joinedFromElement, copy );
			return copy;
		}

		@Override
		public Object visitQualifiedAttributeJoinFromElement(QualifiedAttributeJoinFromElement joinedFromElement) {
			final QualifiedAttributeJoinFromElement existingCopy = (QualifiedAttributeJoinFromElement) fromElementCopyMap.get( joinedFromElement );
			if ( existingCopy != null ) {
				return existingCopy;
			}

			if ( currentFromElementSpaceCopy == null ) {
				throw new ParsingException( "Current FromElementSpace copy was null" );
			}

			QualifiedAttributeJoinFromElement copy = new QualifiedAttributeJoinFromElement(
					currentFromElementSpaceCopy,
					joinedFromElement.getUniqueIdentifier(),
					joinedFromElement.getIdentificationVariable(),
					joinedFromElement.getJoinedAttributeDescriptor(),
					joinedFromElement.getIntrinsicSubclassIndicator(),
					joinedFromElement.asLoggableText(),
					joinedFromElement.getJoinType(),
					joinedFromElement,
					joinedFromElement.isFetched()
			);
			fromElementCopyMap.put( joinedFromElement, copy );
			return copy;
		}

		@Override
		public SqmSelectClause visitSelectClause(SqmSelectClause selectClause) {
			SqmSelectClause copy = new SqmSelectClause( selectClause.isDistinct() );
			for ( SqmSelection selection : selectClause.getSelections() ) {
				copy.addSelection( visitSelection( selection ) );
			}
			return copy;
		}

		@Override
		public SqmSelection visitSelection(SqmSelection selection) {
			return new SqmSelection(
					(SqmExpression) selection.getExpression().accept( this ),
					selection.getAlias()
			);
		}

		@Override
		public SqmDynamicInstantiation visitDynamicInstantiation(SqmDynamicInstantiation dynamicInstantiation) {
			SqmDynamicInstantiation copy = dynamicInstantiation.makeShallowCopy();
			for ( SqmDynamicInstantiationArgument aliasedArgument : dynamicInstantiation.getArguments() ) {
				copy.addArgument(
						new SqmDynamicInstantiationArgument(
								(SqmExpression) aliasedArgument.getExpression().accept( this ),
								aliasedArgument.getAlias()
						)
				);
			}
			return copy;
		}

		@Override
		public SqmWhereClause visitWhereClause(SqmWhereClause whereClause) {
			if ( whereClause == null ) {
				return null;
			}
			return new SqmWhereClause( (SqmPredicate) whereClause.getPredicate().accept( this ) );
		}

		@Override
		public GroupedSqmPredicate visitGroupedPredicate(GroupedSqmPredicate predicate) {
			return new GroupedSqmPredicate( (SqmPredicate) predicate.accept( this ) );
		}

		@Override
		public AndSqmPredicate visitAndPredicate(AndSqmPredicate predicate) {
			return new AndSqmPredicate(
					(SqmPredicate) predicate.getLeftHandPredicate().accept( this ),
					(SqmPredicate) predicate.getRightHandPredicate().accept( this )
			);
		}

		@Override
		public OrSqmPredicate visitOrPredicate(OrSqmPredicate predicate) {
			return new OrSqmPredicate(
					(SqmPredicate) predicate.getLeftHandPredicate().accept( this ),
					(SqmPredicate) predicate.getRightHandPredicate().accept( this )
			);
		}

		@Override
		public RelationalSqmPredicate visitRelationalPredicate(RelationalSqmPredicate predicate) {
			return new RelationalSqmPredicate(
					predicate.getOperator(),
					(SqmExpression) predicate.getLeftHandExpression().accept( this ),
					(SqmExpression) predicate.getRightHandExpression().accept( this )
			);
		}

		@Override
		public EmptinessSqmPredicate visitIsEmptyPredicate(EmptinessSqmPredicate predicate) {
			return new EmptinessSqmPredicate(
					(SqmExpression) predicate.getExpression().accept( this ),
					predicate.isNegated()
			);
		}

		@Override
		public NullnessSqmPredicate visitIsNullPredicate(NullnessSqmPredicate predicate) {
			return new NullnessSqmPredicate(
					(SqmExpression) predicate.getExpression().accept( this ),
					predicate.isNegated()
			);
		}

		@Override
		public BetweenSqmPredicate visitBetweenPredicate(BetweenSqmPredicate predicate) {
			return new BetweenSqmPredicate(
					(SqmExpression) predicate.getExpression().accept( this ),
					(SqmExpression) predicate.getLowerBound().accept( this ),
					(SqmExpression) predicate.getUpperBound().accept( this ),
					predicate.isNegated()
			);
		}

		@Override
		public LikeSqmPredicate visitLikePredicate(LikeSqmPredicate predicate) {
			return new LikeSqmPredicate(
					(SqmExpression) predicate.getMatchExpression().accept( this ),
					(SqmExpression) predicate.getPattern().accept( this ),
					(SqmExpression) predicate.getEscapeCharacter().accept( this )
			);
		}

		@Override
		public MemberOfSqmPredicate visitMemberOfPredicate(MemberOfSqmPredicate predicate) {
			return new MemberOfSqmPredicate(
					visitAttributeReferenceExpression( predicate.getAttributeReferenceExpression() )
			);
		}

		@Override
		public NegatedSqmPredicate visitNegatedPredicate(NegatedSqmPredicate predicate) {
			return new NegatedSqmPredicate(
					(SqmPredicate) predicate.getWrappedPredicate().accept( this )
			);
		}

		@Override
		public InListSqmPredicate visitInListPredicate(InListSqmPredicate predicate) {
			InListSqmPredicate copy = new InListSqmPredicate(
					(SqmExpression) predicate.getTestExpression().accept( this )
			);
			for ( SqmExpression expression : predicate.getListExpressions() ) {
				copy.addExpression( (SqmExpression) expression.accept( this ) );
			}
			return copy;
		}

		@Override
		public InSubQuerySqmPredicate visitInSubQueryPredicate(InSubQuerySqmPredicate predicate) {
			return new InSubQuerySqmPredicate(
					(SqmExpression) predicate.getTestExpression().accept( this ),
					visitSubQueryExpression( predicate.getSubQueryExpression() )
			);
		}

		@Override
		public OrderByClause visitOrderByClause(OrderByClause orderByClause) {
			if ( orderByClause == null ) {
				return null;
			}

			OrderByClause copy = new OrderByClause();
			for ( SortSpecification sortSpecification : orderByClause.getSortSpecifications() ) {
				copy.addSortSpecification( visitSortSpecification( sortSpecification ) );
			}
			return copy;
		}

		@Override
		public SortSpecification visitSortSpecification(SortSpecification sortSpecification) {
			return new SortSpecification(
					(SqmExpression) sortSpecification.getSortExpression().accept( this ),
					sortSpecification.getCollation(),
					sortSpecification.getSortOrder()
			);
		}

		@Override
		public PositionalParameterSqmExpression visitPositionalParameterExpression(PositionalParameterSqmExpression expression) {
			return new PositionalParameterSqmExpression( expression.getPosition(), expression.allowMultiValuedBinding() );
		}

		@Override
		public NamedParameterSqmExpression visitNamedParameterExpression(NamedParameterSqmExpression expression) {
			return new NamedParameterSqmExpression( expression.getName(), expression.allowMultiValuedBinding() );
		}

		@Override
		public EntityTypeSqmExpression visitEntityTypeExpression(EntityTypeSqmExpression expression) {
			return new EntityTypeSqmExpression( expression.getExpressionType() );
		}

		@Override
		public UnaryOperationSqmExpression visitUnaryOperationExpression(UnaryOperationSqmExpression expression) {
			return new UnaryOperationSqmExpression(
					expression.getOperation(),
					(SqmExpression) expression.getOperand().accept( this )
			);
		}

		@Override
		public AttributeReferenceSqmExpression visitAttributeReferenceExpression(AttributeReferenceSqmExpression expression) {
			AttributeBindingSource attributeBindingSource = expression.getAttributeBindingSource();
			if ( attributeBindingSource instanceof FromElement ) {
				// find the FromElement copy
				final FromElement sourceCopy = fromElementCopyMap.get( attributeBindingSource );
				if ( sourceCopy == null ) {
					throw new AssertionError( "FromElement not found in copy map" );
				}
				attributeBindingSource = sourceCopy;
			}
			return new AttributeReferenceSqmExpression(
					attributeBindingSource,
					expression.getBoundAttribute()
			);
		}

		@Override
		public GenericFunctionSqmExpression visitGenericFunction(GenericFunctionSqmExpression expression) {
			List<SqmExpression> argumentsCopy = new ArrayList<SqmExpression>();
			for ( SqmExpression argument : expression.getArguments() ) {
				argumentsCopy.add( (SqmExpression) argument.accept( this ) );
			}
			return new GenericFunctionSqmExpression(
					expression.getFunctionName(),
					expression.getExpressionType(),
					argumentsCopy
			);
		}

		@Override
		public AvgFunctionSqmExpression visitAvgFunction(AvgFunctionSqmExpression expression) {
			return new AvgFunctionSqmExpression(
					(SqmExpression) expression.getArgument().accept( this ),
					expression.isDistinct(),
					expression.getExpressionType()
			);
		}

		@Override
		public CountStarFunctionSqmExpression visitCountStarFunction(CountStarFunctionSqmExpression expression) {
			return new CountStarFunctionSqmExpression( expression.isDistinct(), expression.getExpressionType() );
		}

		@Override
		public CountFunctionSqmExpression visitCountFunction(CountFunctionSqmExpression expression) {
			return new CountFunctionSqmExpression(
					(SqmExpression) expression.getArgument().accept( this ),
					expression.isDistinct(),
					expression.getExpressionType()
			);
		}

		@Override
		public MaxFunctionSqmExpression visitMaxFunction(MaxFunctionSqmExpression expression) {
			return new MaxFunctionSqmExpression(
					(SqmExpression) expression.getArgument().accept( this ),
					expression.isDistinct(),
					expression.getExpressionType()
			);
		}

		@Override
		public MinFunctionSqmExpression visitMinFunction(MinFunctionSqmExpression expression) {
			return new MinFunctionSqmExpression(
					(SqmExpression) expression.getArgument().accept( this ),
					expression.isDistinct(),
					expression.getExpressionType()
			);
		}

		@Override
		public SumFunctionSqmExpression visitSumFunction(SumFunctionSqmExpression expression) {
			return new SumFunctionSqmExpression(
					(SqmExpression) expression.getArgument().accept( this ),
					expression.isDistinct(),
					expression.getExpressionType()
			);
		}

		@Override
		public LiteralStringSqmExpression visitLiteralStringExpression(LiteralStringSqmExpression expression) {
			return new LiteralStringSqmExpression(
					expression.getLiteralValue(),
					expression.getExpressionType()
			);
		}

		@Override
		public LiteralCharacterSqmExpression visitLiteralCharacterExpression(LiteralCharacterSqmExpression expression) {
			return new LiteralCharacterSqmExpression(
					expression.getLiteralValue(),
					expression.getExpressionType()
			);
		}

		@Override
		public LiteralDoubleSqmExpression visitLiteralDoubleExpression(LiteralDoubleSqmExpression expression) {
			return new LiteralDoubleSqmExpression(
					expression.getLiteralValue(),
					expression.getExpressionType()
			);
		}

		@Override
		public LiteralIntegerSqmExpression visitLiteralIntegerExpression(LiteralIntegerSqmExpression expression) {
			return new LiteralIntegerSqmExpression(
					expression.getLiteralValue(),
					expression.getExpressionType()
			);
		}

		@Override
		public LiteralBigIntegerSqmExpression visitLiteralBigIntegerExpression(LiteralBigIntegerSqmExpression expression) {
			return new LiteralBigIntegerSqmExpression(
					expression.getLiteralValue(),
					expression.getExpressionType()
			);
		}

		@Override
		public LiteralBigDecimalSqmExpression visitLiteralBigDecimalExpression(LiteralBigDecimalSqmExpression expression) {
			return new LiteralBigDecimalSqmExpression(
					expression.getLiteralValue(),
					expression.getExpressionType()
			);
		}

		@Override
		public LiteralFloatSqmExpression visitLiteralFloatExpression(LiteralFloatSqmExpression expression) {
			return new LiteralFloatSqmExpression(
					expression.getLiteralValue(),
					expression.getExpressionType()
			);
		}

		@Override
		public LiteralLongSqmExpression visitLiteralLongExpression(LiteralLongSqmExpression expression) {
			return new LiteralLongSqmExpression(
					expression.getLiteralValue(),
					expression.getExpressionType()
			);
		}

		@Override
		public LiteralTrueSqmExpression visitLiteralTrueExpression(LiteralTrueSqmExpression expression) {
			return new LiteralTrueSqmExpression( expression.getExpressionType() );
		}

		@Override
		public LiteralFalseSqmExpression visitLiteralFalseExpression(LiteralFalseSqmExpression expression) {
			return new LiteralFalseSqmExpression( expression.getExpressionType() );
		}

		@Override
		public LiteralNullSqmExpression visitLiteralNullExpression(LiteralNullSqmExpression expression) {
			return new LiteralNullSqmExpression();
		}

		@Override
		public ConcatSqmExpression visitConcatExpression(ConcatSqmExpression expression) {
			return new ConcatSqmExpression(
					(SqmExpression) expression.getLeftHandOperand().accept( this ),
					(SqmExpression) expression.getRightHandOperand().accept( this )
			);
		}

		@Override
		public ConcatFunctionSqmExpression visitConcatFunction(ConcatFunctionSqmExpression expression) {
			final List<SqmExpression> arguments = new ArrayList<SqmExpression>();
			for ( SqmExpression argument : expression.getExpressions() ) {
				arguments.add( (SqmExpression) argument.accept( this ) );
			}

			return new ConcatFunctionSqmExpression( expression.getFunctionResultType(), arguments );
		}

		@Override
		@SuppressWarnings("unchecked")
		public ConstantEnumSqmExpression visitConstantEnumExpression(ConstantEnumSqmExpression expression) {
			return new ConstantEnumSqmExpression( expression.getValue(), expression.getExpressionType() );
		}

		@Override
		@SuppressWarnings("unchecked")
		public ConstantFieldSqmExpression visitConstantFieldExpression(ConstantFieldSqmExpression expression) {
			return new ConstantFieldSqmExpression( expression.getValue(), expression.getExpressionType() );
		}

		@Override
		public BinaryArithmeticSqmExpression visitBinaryArithmeticExpression(BinaryArithmeticSqmExpression expression) {
			return new BinaryArithmeticSqmExpression(
					expression.getOperation(),
					(SqmExpression) expression.getLeftHandOperand().accept( this ),
					(SqmExpression) expression.getRightHandOperand().accept( this ),
					expression.getExpressionType()
			);
		}

		@Override
		public SubQuerySqmExpression visitSubQueryExpression(SubQuerySqmExpression expression) {
			return new SubQuerySqmExpression(
					visitQuerySpec( expression.getQuerySpec() ),
					// assume already validated
					expression.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression().getExpressionType()
			);
		}
	}

}
