/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */

/**
 * API for parsing HQL/JPQL queries and JPA Criteria queries into Semantic Query Model (SQM) representation.  The
 * main entry point into the parsing is {@link org.hibernate.sqm.SemanticQueryInterpreter}.
 * <p/>
 * For HQL/JPQL parsing, pass in the sqm string and a {@link org.hibernate.sqm.ConsumerContext} and get
 * back the semantic sqm tree as a {@link org.hibernate.sqm.query.Statement}.
 * <p/>
 * For Criteria queries ...
 * <p/>
 * Generally, the parser will throw exceptions as one of 3 types:<ul>
 *     <li>
 *         {@link org.hibernate.sqm.parser.QueryException} and derivatives represent problems with the
 *         sqm itself.
 *     </li>
 *     <li>
 *         {@link org.hibernate.sqm.parser.ParsingException} and derivatives represent errors (potential bugs)
 *         during parsing.
 *     </li>
 *     <li>
 *         {@link org.hibernate.sqm.parser.InterpretationException} represents an unexpected problem during
 *         interpretation; this may indicate a problem with the sqm or a bug in the parser, we just are not
 *         sure as it was unexpected.
 *     </li>
 * </ul>
 */
package org.hibernate.sqm.parser;
