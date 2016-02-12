 /**
 * Copyright (C) 2010-2016 Structr GmbH
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.core.parser.function;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.schema.action.ActionContext;
import org.structr.schema.action.Function;

public class TimerFunction extends Function<Object, Object>{

	public static final String ERROR_MESSAGE_TIMER = "Usage: ${timer(name, action)}. Example: ${timer('benchmark1', 'start')}";
	public static final String ERROR_MESSAGE_TIMER_JS = "Usage: ${{Structr.timer(name, action)}}. Example: ${{Structr.timer('benchmark1', 'start')}}";

	private static final Logger logger = Logger.getLogger(ChangelogFunction.class.getName());


	@Override
	public Object apply(final ActionContext ctx, final GraphObject entity, final Object[] sources) throws FrameworkException {

		if (sources.length == 2 && sources[0] instanceof String && sources[1] instanceof String) {

			final String name = sources[0].toString();
			final String action = sources[1].toString();

			if (action.equals("start")) {

				ctx.addTimer(name);

				return null;

			} else if (action.equals("get")) {

				Date begin = ctx.getTimer(name);

				if (begin == null) {

					logger.log(Level.WARNING, "Timer {0} has not been started yet. Starting it.", name);

					ctx.addTimer(name);

					return 0;

				} else {

					return (new Date()).getTime() -  begin.getTime();

				}

			} else {

				logger.log(Level.WARNING, "Unknown action for timer function: {0}", action);

			}

		}

		return usage(ctx.isJavaScriptContext());
	}

	@Override
	public String usage(boolean inJavaScriptContext) {
		return (inJavaScriptContext ? ERROR_MESSAGE_TIMER_JS : ERROR_MESSAGE_TIMER);
	}

	@Override
	public String shortDescription() {
		return "Starts/Stops/Pings a timer";
	}

	@Override
	public String getName() {
		return "timer()";
	}

}
