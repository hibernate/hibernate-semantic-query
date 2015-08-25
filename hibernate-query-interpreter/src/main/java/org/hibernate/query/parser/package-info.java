/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/**
 * API for parsing HQL/JPQL queries and JPA Criteria queries into Semantic Query Model (SQM) representation.  The
 * main entry point into the parsing is {@link org.hibernate.query.parser.SemanticQueryInterpreter}.
 * <p/>
 * For HQL/JPQL parsing, pass in the query string and a {@link org.hibernate.query.parser.ConsumerContext} and get
 * back the semantic query tree as a {@link org.hibernate.sqm.query.Statement}.
 * <p/>
 * For Criteria queries ...
 * <p/>
 * Generally, the parser will throw exceptions as one of 2 types:<ul>
 *     <li>
 *         {@link org.hibernate.query.parser.QueryException} and derivatives represent problems with the
 *         query itself.
 *     </li>
 *     <li>
 *         {@link org.hibernate.query.parser.ParsingException} and derivatives represent errors (potential bugs)
 *         during parsing.
 *     </li>
 * </ul>
 */
package org.hibernate.query.parser;
