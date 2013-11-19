/**
 * Copyright (C) 2010-2013 Axel Morgner, structr <structr@structr.org>
 *
 * This file is part of structr <http://structr.org>.
 *
 * structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.structr.web.entity.html.relation;

import org.structr.core.property.Property;
import org.structr.common.PropertyView;
import org.structr.common.View;
import org.structr.core.entity.ManyToOne;
import org.structr.core.property.StringProperty;
import org.structr.web.entity.Linkable;
import org.structr.web.entity.html.Link;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author Christian Morgner
 */
public class ResourceLink extends ManyToOne<Link, Linkable> {

	public static final Property<String> sourceId = new StringProperty("sourceId");
	public static final Property<String> targetId = new StringProperty("targetId");
	public static final Property<String> type     = new StringProperty("type");

	public static final View uiView = new View(ResourceLink.class, PropertyView.Ui,
		sourceId, targetId, type	
	);
	
	static {

		// EntityContext.registerNamedRelation("resource_link", ResourceLink.class, Link.class, Linkable.class, RelType.LINK);
		// EntityContext.registerNamedRelation("hyperlink", ResourceLink.class, A.class, Linkable.class, RelType.LINK);
	}

	@Override
	public Class<Link> getSourceType() {
		return Link.class;
	}

	@Override
	public String name() {
		return "LINK";
	}

	@Override
	public Class<Linkable> getTargetType() {
		return Linkable.class;
	}

	@Override
	public Property<String> getSourceIdProperty() {
		return sourceId;
	}

	@Override
	public Property<String> getTargetIdProperty() {
		return targetId;
	}
}
