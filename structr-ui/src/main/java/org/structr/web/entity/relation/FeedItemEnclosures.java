/**
 * Copyright (C) 2010-2016 Structr GmbH
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
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.web.entity.relation;

import org.structr.core.entity.OneToMany;
import org.structr.core.entity.Relation;
import org.structr.web.entity.feed.FeedItem;
import org.structr.web.entity.feed.FeedItemEnclosure;

/**
 *
 * @author Christian Kramp <christian.kramp@structr.com>
 */
public class FeedItemEnclosures extends OneToMany<FeedItem, FeedItemEnclosure> {

	@Override
	public Class<FeedItem> getSourceType() {
		return FeedItem.class;
	}

	@Override
	public Class<FeedItemEnclosure> getTargetType() {
		return FeedItemEnclosure.class;
	}

	@Override
	public String name() {
		return "FEED_ITEM_ENCLOSURES";
	}

	@Override
	public int getCascadingDeleteFlag() {
		return Relation.SOURCE_TO_TARGET;
	}
}
