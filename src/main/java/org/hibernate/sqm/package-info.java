/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

/**
 * Main contact point for semantic query interpretation is
 * {@link org.hibernate.sqm.SemanticQueryInterpreter}.
 * <p/>
 * Once you have received a {@link org.hibernate.sqm.query.SqmStatement} from
 * SemanticQueryInterpreter you can:<ul>
 *     <li>
 *         "Split" it (if it is a {@link org.hibernate.sqm.query.SqmSelectStatement})
 *         using {@link org.hibernate.sqm.QuerySplitter}
 *     </li>
 *     <li>
 *         Create a walker/visitor for it using {@link org.hibernate.sqm.BaseSemanticQueryWalker}
 *     </li>
 * </ul>
 */
package org.hibernate.sqm;
