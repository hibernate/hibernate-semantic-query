/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

/**
 * The hibernate-semantic-query project (or SQM for short) aims to
 * define a reusable "black box" for HQL interpretation.  HQL is a
 * super-set of JPA's JPQL language, therefore this project can also
 * interpret JPQL queries by definition.
 * <p/>
 * Main contact point for semantic query interpretation is
 * {@link org.hibernate.query.sqm.produce.spi.SemanticQueryProducer}.
 * <p/>
 * Once you have received a {@link org.hibernate.query.sqm.tree.SqmStatement} from
 * SemanticQueryInterpreter you can:<ul>
 *     <li>
 *         "Split" it (if it is a {@link org.hibernate.query.sqm.tree.SqmSelectStatement})
 *         using {@link org.hibernate.query.sqm.consume.spi.QuerySplitter}
 *     </li>
 *     <li>
 *         Create a walker/visitor for it using {@link org.hibernate.query.sqm.consume.spi.BaseSemanticQueryWalker}
 *     </li>
 * </ul>
 * <p/>
 * NOTE: the choice to prefix all SQM AST node class names with <b>Sqm</b> was
 * made to help make consumers easier to write - consumers are likely to
 * have classes with the same purpose in their tree (e.g. the notion of a
 * "from clause").  But if both projects define a class named {@code FromClause}
 * then the consumer would have to fully qualify one reference to distinguish.
 * That makes for overly long code.  Prefixing the names of classes from this
 * project alleviates that problem.
 */
package org.hibernate.sqm;
