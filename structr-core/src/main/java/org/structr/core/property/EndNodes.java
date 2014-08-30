/**
 * Copyright (C) 2010-2014 Morgner UG (haftungsbeschränkt)
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.core.property;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.BooleanClause;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.Iterables;
import org.structr.common.NotNullPredicate;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.app.App;
import org.structr.core.app.Query;
import org.structr.core.app.StructrApp;
import org.structr.core.converter.PropertyConverter;
import org.structr.core.entity.ManyEndpoint;
import org.structr.core.entity.Relation;
import org.structr.core.entity.Source;
import org.structr.core.graph.NodeInterface;
import org.structr.core.graph.NodeService;
import org.structr.core.graph.search.EmptySearchAttribute;
import org.structr.core.graph.search.SearchAttribute;
import org.structr.core.graph.search.SourceSearchAttribute;
import org.structr.core.notion.Notion;
import org.structr.core.notion.ObjectNotion;

/**
 * A property that defines a relationship with the given parameters between a node and a collection of other nodes.
 *
 * @author Christian Morgner
 */
public class EndNodes<S extends NodeInterface, T extends NodeInterface> extends Property<List<T>> implements RelationProperty<T> {

	private static final Logger logger = Logger.getLogger(EndNodes.class.getName());

	private Relation<S, T, ? extends Source, ManyEndpoint<T>> relation = null;
	private Notion notion                                              = null;
	private Class<T> destType                                          = null;

	/**
	 * Constructs a collection property with the given name, the given destination type and the given relationship type.
	 *
	 * @param name
	 * @param destType
	 * @param relType
	 */
	public EndNodes(final String name, final Class<? extends Relation<S, T, ? extends Source, ManyEndpoint<T>>> relationClass) {
		this(name, relationClass, new ObjectNotion());
	}

	/**
	 * Constructs a collection property with the given name, the given destination type and the given relationship type.
	 *
	 * @param name
	 * @param destType
	 * @param relType
	 */
	public EndNodes(final String name, final Class<? extends Relation<S, T, ? extends Source, ManyEndpoint<T>>> relationClass, final Notion notion) {

		super(name);

		try {

			this.relation = relationClass.newInstance();

		} catch (Throwable t) {
			t.printStackTrace();
		}

		this.notion   = notion;
		this.destType = relation.getTargetType();

		this.notion.setType(destType);

		StructrApp.getConfiguration().registerConvertedProperty(this);
	}

	@Override
	public String typeName() {
		return "Object";
	}

	@Override
	public Integer getSortType() {
		return null;
	}

	@Override
	public PropertyConverter<List<T>, ?> databaseConverter(SecurityContext securityContext) {
		return null;
	}

	@Override
	public PropertyConverter<List<T>, ?> databaseConverter(SecurityContext securityContext, GraphObject entity) {
		return null;
	}

	@Override
	public PropertyConverter<?, List<T>> inputConverter(SecurityContext securityContext) {
		return getNotion().getCollectionConverter(securityContext);
	}

	@Override
	public List<T> getProperty(SecurityContext securityContext, GraphObject obj, boolean applyConverter) {
		return getProperty(securityContext, obj, applyConverter, null);
	}

	@Override
	public List<T> getProperty(SecurityContext securityContext, GraphObject obj, boolean applyConverter, final org.neo4j.helpers.Predicate<GraphObject> predicate) {

		ManyEndpoint<T> endpoint = relation.getTarget();

		if (predicate != null) {

			return Iterables.toList(Iterables.filter(predicate, Iterables.filter(new NotNullPredicate(), endpoint.get(securityContext, (NodeInterface)obj, null))));

		} else {

			return Iterables.toList(Iterables.filter(new NotNullPredicate(), endpoint.get(securityContext, (NodeInterface)obj, null)));
		}
	}

	@Override
	public void setProperty(SecurityContext securityContext, GraphObject obj, List<T> collection) throws FrameworkException {

		ManyEndpoint<T> endpoint = relation.getTarget();

		endpoint.set(securityContext, (NodeInterface)obj, collection);
	}

	@Override
	public Class relatedType() {
		return destType;
	}

	@Override
	public boolean isCollection() {
		return true;
	}

	@Override
	public Property<List<T>> indexed() {
		return this;
	}

	@Override
	public Property<List<T>> indexed(NodeService.NodeIndex nodeIndex) {
		return this;
	}

	@Override
	public Property<List<T>> indexed(NodeService.RelationshipIndex relIndex) {
		return this;
	}

	@Override
	public Property<List<T>> passivelyIndexed() {
		return this;
	}

	@Override
	public Property<List<T>> passivelyIndexed(NodeService.NodeIndex nodeIndex) {
		return this;
	}

	@Override
	public Property<List<T>> passivelyIndexed(NodeService.RelationshipIndex relIndex) {
		return this;
	}

	@Override
	public Object fixDatabaseProperty(Object value) {
		return null;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}

	@Override
	public void index(GraphObject entity, Object value) {
		// no indexing
	}

	@Override
	public Object getValueForEmptyFields() {
		return null;
	}

	// ----- interface RelationProperty -----
	@Override
	public Notion getNotion() {
		return notion;
	}

	@Override
	public void addSingleElement(final SecurityContext securityContext, final GraphObject obj, final T t) throws FrameworkException {

		List<T> list = getProperty(securityContext, obj, false);
		list.add(t);

		setProperty(securityContext, obj, list);
	}

	@Override
	public Class<T> getTargetType() {
		return destType;
	}

	@Override
	public List<T> convertSearchValue(SecurityContext securityContext, String requestParameter) throws FrameworkException {

		final PropertyConverter inputConverter = inputConverter(securityContext);
		if (inputConverter != null) {

			final List<String> sources = new LinkedList<>();
			if (requestParameter != null) {

				for (String part : requestParameter.split("[,;]+")) {
					sources.add(part);
				}
			}

			return (List<T>)inputConverter.convert(sources);
		}

		return null;
	}

	@Override
	public SearchAttribute getSearchAttribute(SecurityContext securityContext, BooleanClause.Occur occur, List<T> searchValue, boolean exactMatch, final Query query) {

		final Predicate<GraphObject> predicate    = query != null ? query.toPredicate() : null;
		final SourceSearchAttribute attr          = new SourceSearchAttribute(occur);
		final Set<GraphObject> intersectionResult = new LinkedHashSet<>();
		boolean alreadyAdded                      = false;

		if (searchValue != null && !StringUtils.isBlank(searchValue.toString())) {

			final App app = StructrApp.getInstance(securityContext);

			if (exactMatch) {

				for (NodeInterface node : searchValue) {

					switch (occur) {

						case MUST:

							if (!alreadyAdded) {

								// the first result is the basis of all subsequent intersections
								intersectionResult.addAll(getRelatedNodesReverse(securityContext, node, declaringClass, predicate));

								// the next additions are intersected with this one
								alreadyAdded = true;

							} else {

								intersectionResult.retainAll(getRelatedNodesReverse(securityContext, node, declaringClass, predicate));
							}

							break;

						case SHOULD:
							intersectionResult.addAll(getRelatedNodesReverse(securityContext, node, declaringClass, predicate));
							break;

						case MUST_NOT:
							break;
					}
				}

			} else {

				// loose search behaves differently, all results must be combined
				for (NodeInterface node : searchValue) {

					intersectionResult.addAll(getRelatedNodesReverse(securityContext, node, declaringClass, predicate));
				}
			}

			attr.setResult(intersectionResult);

		} else {

			// experimental filter attribute that
			// removes entities with a non-empty
			// value in the given field
			return new EmptySearchAttribute(this, null);
		}

		return attr;
	}

	// ----- overridden methods from super class -----
	@Override
	protected <T extends NodeInterface> Set<T> getRelatedNodesReverse(final SecurityContext securityContext, final NodeInterface obj, final Class destinationType, final Predicate<GraphObject> predicate) {

		Set<T> relatedNodes = new LinkedHashSet<>();

		try {

			final Object source = relation.getSource().get(securityContext, obj, predicate);
			if (source instanceof Iterable) {

				Iterable<T> nodes = (Iterable<T>)source;
				for (final T n : nodes) {

					relatedNodes.add(n);
				}

			} else {

				relatedNodes.add((T)source);
			}

		} catch (Throwable t) {

			logger.log(Level.WARNING, "Unable to fetch related node: {0}", t.getMessage());
		}

		return relatedNodes;
	}

	@Override
	public Relation getRelation() {
		return relation;
	}
}