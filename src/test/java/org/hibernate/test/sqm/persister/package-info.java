/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

/**
 * Meant as a prototype of the model to use (and how the SQM domain model stuff fits in)
 *
 * Also wanted to note here that how resolution of {@link org.hibernate.sqm.domain.SqmNavigable}
 * is now handled (via visitation pattern) through the {@link org.hibernate.sqm.domain.SqmNavigable#makeBinding}
 * I believe means we can drop the specific sub-interfaces of {@link org.hibernate.test.sqm.type.spi.Attribute}
 * beyond just the 2 {@link org.hibernate.test.sqm.type.spi.SingularSqmAttribute} and
 * {@link org.hibernate.test.sqm.domain.PluralSqmAttribute}.  On the ORM side we would need the distinct impls
 * anyway to properly handle the {@code #makeBinding} visitation.  Those things would already handle building
 * the proper bindings that we can then access later when processing the SQM tree.
 */
package org.hibernate.test.sqm.persister;
