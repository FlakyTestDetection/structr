/**
 * Copyright (C) 2010-2017 Structr GmbH
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
package org.structr.web.function;

import org.structr.common.error.FrameworkException;
import org.structr.schema.action.ActionContext;
import org.structr.schema.action.Function;
import static org.structr.web.function.SetSessionAttributeFunction.SESSION_ATTRIBUTE_PREFIX;

/**
 *
 */
public class RemoveSessionAttributeFunction extends Function<Object, Object> {

	public static final String ERROR_MESSAGE_REMOVE_SESSION_ATTRIBUTE    = "Usage: ${remove_session_attribute(key)}. Example: ${remove_session_attribute(\"do_no_track\")}";
	public static final String ERROR_MESSAGE_REMOVE_SESSION_ATTRIBUTE_JS = "Usage: ${{Structr.remove_session_attribute(key)}}. Example: ${{Structr.remove_session_attribute(\"do_not_track\")}}";

	@Override
	public String getName() {
		return "remove_session_attribute()";
	}

	@Override
	public Object apply(final ActionContext ctx, final Object caller, final Object[] sources) throws FrameworkException {

		try {
			
			if (!arrayHasLengthAndAllElementsNotNull(sources, 1)) {
				
				return null;
			}

			ctx.getSecurityContext().getSession().removeAttribute(SESSION_ATTRIBUTE_PREFIX.concat(sources[0].toString()));
			

		} catch (final IllegalArgumentException e) {

			logParameterError(caller, sources, ctx.isJavaScriptContext());

			return usage(ctx.isJavaScriptContext());

		}
		
		return "";

	}

	@Override
	public String usage(boolean inJavaScriptContext) {
		return (inJavaScriptContext ? ERROR_MESSAGE_REMOVE_SESSION_ATTRIBUTE_JS : ERROR_MESSAGE_REMOVE_SESSION_ATTRIBUTE);
	}

	@Override
	public String shortDescription() {
		return "";
	}

}