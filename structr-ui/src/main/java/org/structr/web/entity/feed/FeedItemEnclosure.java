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
package org.structr.web.entity.feed;

import java.io.InputStream;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.structr.common.PropertyView;
import org.structr.common.SecurityContext;
import org.structr.common.View;
import org.structr.core.Export;
import org.structr.core.GraphObject;
import static org.structr.core.GraphObject.type;
import org.structr.core.app.StructrApp;
import org.structr.core.entity.AbstractNode;
import static org.structr.core.graph.NodeInterface.owner;
import org.structr.core.property.LongProperty;
import org.structr.core.property.Property;
import org.structr.core.property.StartNode;
import org.structr.core.property.StringProperty;
import org.structr.files.text.FulltextIndexingTask;
import org.structr.web.common.DownloadHelper;
import org.structr.web.entity.Indexable;
import static org.structr.web.entity.Indexable.contentType;
import static org.structr.web.entity.Indexable.extractedContent;
import static org.structr.web.entity.Indexable.indexedWords;
import org.structr.web.entity.relation.FeedItemContents;

/**
 * Represents feed enclosures
 * @author Christian Kramp <christian.kramp@structr.com>
 */
public class FeedItemEnclosure extends AbstractNode implements Indexable {

    private static final Logger logger = Logger.getLogger(FeedItemContent.class.getName());

    public static final Property<String> url                     = new StringProperty("url");
    public static final Property<Long> enclosureLength                  = new LongProperty("enclosureLength");
    public static final Property<String> enclosureType             = new StringProperty("enclosureType");
    public static final Property<FeedItem> item                 = new StartNode<>("item", FeedItemContents.class);

    public static final View publicView = new View(FeedItemContent.class, PropertyView.Public, type, contentType, owner,
            url, enclosureLength, enclosureType, item);
    public static final View uiView     = new View(FeedItemContent.class, PropertyView.Ui, type, contentType, owner, extractedContent, indexedWords,
            url, enclosureLength, enclosureType, item);

	@Override
	public void afterCreation(SecurityContext securityContext) {

		try {

			StructrApp.getInstance(securityContext).processTasks(new FulltextIndexingTask(this));
			
		} catch (Throwable t) {

		}

	}

	@Export
	@Override
	public GraphObject getSearchContext(final String searchTerm, final int contextLength) {

		final String text = getProperty(url);
		if (text != null) {

			return DownloadHelper.getContextObject(searchTerm, text, contextLength);
		}

		return null;
	}

	@Override
	public InputStream getInputStream() {
		
		return IOUtils.toInputStream(getProperty(url));
	}
}
