/*
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
var ws;
var loggedIn = false, isAdmin = false;
var user, me;
var reconn, ping;

var rawResultCount = [];
var pageCount = [];
var page = 1;
var pageSize = 25;
var sort = 'name';
var order = 'asc';

var footer = $('#footer');

var rootUrl = '/structr/rest/';
var csvRootUrl = '/structr/csv/';
var viewRootUrl = '/';
var wsRoot = '/structr/ws';
var port = document.location.port;

function wsConnect() {

	_Logger.log(_LogType.WEBSOCKET, '################ Global connect() ################', ws, ws ? ws.readyState : '');

	try {

		if (!ws || ws.readyState > 1) {
			// closed websocket

			if (ws) {
				ws.onopen    = undefined;
				ws.onclose   = undefined;
				ws.onmessage = undefined;
				ws = undefined;
			}

			var isEnc = (window.location.protocol === 'https:');
			var host = document.location.host;
			var wsUrl = 'ws' + (isEnc ? 's' : '') + '://' + host + wsRoot;

			_Logger.log(_LogType.WEBSOCKET, wsUrl);
			if ('WebSocket' in window) {

				try {
					ws = new WebSocket(wsUrl, 'structr');
				} catch (e) {}

			} else if ('MozWebSocket' in window) {

				try {
					ws = new MozWebSocket(wsUrl, 'structr');
				} catch (e) {}

			} else {

				alert('Your browser doesn\'t support WebSocket.');
				return false;

			}
		}

		ws.onopen = function () {

			_Logger.log(_LogType.WEBSOCKET, '############### WebSocket onopen ###############');

			if ($.unblockUI) {
				$.unblockUI({
					fadeOut: 25
				});
			}

			_Logger.log(_LogType.WEBSOCKET, 'de-activating reconnect loop', reconn);
			Structr.stopReconnect();

			Structr.init();

		};

		ws.onclose = function () {

			_Logger.log(_LogType.WEBSOCKET, '############### WebSocket onclose ###############', reconn);

			if (reconn) {
				_Logger.log(_LogType.WEBSOCKET, 'Automatic reconnect already active');
				return;
			}

			// Delay reconnect dialog to prevent it popping up before page reload
			window.setTimeout(function () {

				main.empty();

				var restoreDialogText = '';
				var dialogData = JSON.parse(LSWrapper.getItem(dialogDataKey));
				if (dialogData && dialogData.text) {
					restoreDialogText = '<br><br>The dialog<br><b>"' + dialogData.text + '"</b><br> will be restored after reconnect.';
				}
				Structr.reconnectDialog('<b>Connection lost or timed out.</b><br><br>Don\'t reload the page!' + restoreDialogText + '<br><br>Trying to reconnect... <img class="al" src="' + _Icons.getSpinnerImageAsData() + '">');

				_Logger.log(_LogType.WEBSOCKET, 'ws onclose');
				Structr.reconnect();

			}, 100);

		};

		ws.onmessage = function (message) {

			var data = JSON.parse(message.data);
			var type = data.data.type;
			var command = data.command;
			var msg = data.message;
			var result = data.result;
			var sessionValid = data.sessionValid;
			var code = data.code;

			_Logger.log(_LogType.WS[command], 'ws.onmessage:', data);
			_Logger.log(_LogType.WS[command], '####################################### ', command, ' #########################################');

			if (command === 'LOGIN' || code === 100) {

				me = data.data;
				_Dashboard.checkAdmin();
				isAdmin = data.data.isAdmin;

				_Logger.log(_LogType.WS[command], code, 'user:', user, 'session valid:', sessionValid, 'isAdmin', isAdmin);

				if (!sessionValid) {
					Structr.clearMain();
					Structr.login(msg);
				} else if (!user || user !== data.data.username || loginBox.is(':visible')) {
					Structr.updateUsername(data.data.username);
					loginBox.hide();
					loginBox.find('#usernameField').val('');
					loginBox.find('#passwordField').val('');
					loginBox.find('#errorText').empty();
					Structr.refreshUi();
				}

				StructrModel.callCallback(data.callback, data.data[data.data['key']]);

			} else if (command === 'GET_LOCAL_STORAGE') {

				if (data.data.localStorageString && data.data.localStorageString.length) {
					LSWrapper.setAsJSON(data.data.localStorageString);
				}

				StructrModel.callCallback(data.callback, data.data[data.data['key']]);

			} else if (command === 'CONSOLE') {

				StructrModel.callCallback(data.callback, data);

			} else if (command === 'STATUS') {

				_Logger.log(_LogType.WS[command], 'Error code: ' + code, message);

				if (code === 403) {
					user = null;
					Structr.login('Wrong username or password!');
				} else if (code === 401) {
					user = null;
					Structr.login('');
				} else {

					var codeStr = code ? code.toString() : '';

					if (codeStr === '422') {
						StructrModel.callCallback(data.callback, null, null, true);
					}

					var msgClass;
					if (codeStr.startsWith('2')) {
						msgClass = 'success';
					} else if (codeStr.startsWith('3')) {
						msgClass = 'info';
					} else if (codeStr.startsWith('4')) {
						msgClass = 'warning';
					} else {
						msgClass = 'error';
					}

					if (msg && msg.startsWith('{')) {

						var msgObj = JSON.parse(msg);

						if (dialogBox.is(':visible')) {

							Structr.showAndHideInfoBoxMessage(msgObj.size + ' bytes saved to ' + msgObj.name, msgClass, 2000, 200);

						} else {

							var node = Structr.node(msgObj.id);

							if (node) {

								var progr = node.find('.progress');
								progr.show();

								var size = parseInt(node.find('.size').text());
								var part = msgObj.size;

								node.find('.part').text(part);
								var pw = node.find('.progress').width();
								var w = pw / size * part;

								node.find('.bar').css({width: w + 'px'});

								if (part >= size) {
									blinkGreen(progr);
									window.setTimeout(function () {
										progr.fadeOut('fast');
										_Files.resize();
									}, 1000);
								}
							}
						}

					} else {

						if (codeStr === "404") {
							new MessageBuilder().className(msgClass).text('Object not found.').show();
						} else if (data.error && data.error.errors) {
							Structr.errorFromResponse(data.error);
						} else {
							new MessageBuilder().className(msgClass).text(msg).show();
						}

					}

				}

			} else if (command === 'GET_PROPERTY') {

				_Logger.log(_LogType.WS[command], data.id, data.data['key'], data.data[data.data['key']]);
				StructrModel.updateKey(data.id, data.data['key'], data.data[data.data['key']]);
				StructrModel.callCallback(data.callback, data.data[data.data['key']]);

			} else if (command === 'UPDATE' || command === 'SET_PERMISSION') {

				_Logger.log(_LogType.WS[command], data);

				var obj = StructrModel.obj(data.id);

				if (!obj) {
					data.data.id = data.id;
					obj = StructrModel.create(data.data, null, false);
				}

				obj = StructrModel.update(data);

				StructrModel.callCallback(data.callback, obj);

			} else if (command === 'GET' || command === 'GET_PROPERTIES') {

				StructrModel.callCallback(data.callback, result[0]);

			} else if (command.startsWith('GET') || command === 'GET_BY_TYPE' || command === 'GET_SCHEMA_INFO' || command === 'CREATE_RELATIONSHIP') {

				_Logger.log(_LogType.WS[command], data);

				StructrModel.callCallback(data.callback, result);

			} else if (command === 'CHILDREN') {

				_Logger.log(_LogType.WS[command], data);

				if (result.length > 0 && result[0].name) {
					result.sort(function (a, b) {
						return a.name.localeCompare(b.name);
					});
				}

				var refObject = StructrModel.obj(data.id);

				if (refObject && refObject.constructor.name === 'StructrGroup') {
					result.forEach(function (entity) {
						StructrModel.create(entity, data.id);
					});
				} else {
					result.forEach(function (entity) {
						StructrModel.create(entity);
					});
				}
				StructrModel.callCallback(data.callback, result);

			} else if (command.endsWith('CHILDREN')) {

				_Logger.log(_LogType.WS[command], data);

				$(result).each(function (i, entity) {
					StructrModel.create(entity);
				});

				StructrModel.callCallback(data.callback, result);

			} else if (command.startsWith('SEARCH')) {

				$('.pageCount', $('.pager' + type)).val(pageCount[type]);

				StructrModel.callCallback(data.callback, result, data.rawResultCount);

			} else if (command.startsWith('LIST_UNATTACHED_NODES')) {

				_Logger.log(_LogType.WS[command], result, data);

				StructrModel.callCallback(data.callback, result);

			} else if (command.startsWith('LIST_SCHEMA_PROPERTIES')) {

				_Logger.log(_LogType.WS[command], result, data);

				// send full result in a single callback
				StructrModel.callCallback(data.callback, result);

			} else if (command.startsWith('LIST_COMPONENTS')) {

				_Logger.log(_LogType.WS[command], result, data);

				StructrModel.callCallback(data.callback, result);

			} else if (command.startsWith('LIST_SYNCABLES')) {

				_Logger.log(_LogType.WS[command], result, data);

				StructrModel.callCallback(data.callback, result);

			} else if (command.startsWith('LIST_ACTIVE_ELEMENTS')) {

				_Logger.log(_LogType.WS[command], result, data);

				StructrModel.callCallback(data.callback, result);

			} else if (command.startsWith('SNAPSHOTS')) {

				_Logger.log(_LogType.WS[command], result, data);

				StructrModel.callCallback(data.callback, result);

			} else if (command.startsWith('LAYOUTS')) {

				_Logger.log(_LogType.WS[command], result, data);

				StructrModel.callCallback(data.callback, result);

			} else if (command.startsWith('LIST')) {

				_Logger.log(_LogType.WS[command], result, data);

				StructrModel.callCallback(data.callback, result, data.rawResultCount);

			} else if (command.startsWith('QUERY')) {

				_Logger.log(_LogType.WS[command], result, data);

				StructrModel.callCallback(data.callback, result, data.rawResultCount);

			} else if (command === 'DELETE') {

				StructrModel.del(data.id);

			} else if (command === 'INSERT_BEFORE' || command === 'APPEND_CHILD' || command === 'APPEND_USER') {

				StructrModel.create(result[0], data.data.refId);

			} else if (command.startsWith('APPEND_FILE')) {

				//StructrModel.create(result[0], data.data.refId);

			} else if (command === 'REMOVE') {

				var obj = StructrModel.obj(data.id);
				if (obj) {
					_Logger.log(_LogType.WS[command], 'Remove object from model', obj);
					obj.remove();
				}

			} else if (command === 'REMOVE_CHILD') {

				var obj = StructrModel.obj(data.id);
				if (obj) {
					_Logger.log(_LogType.WS[command], 'Remove object from parent', data, obj);
					obj.remove(data.data.parentId);
				}

			} else if (command === 'CREATE' || command === 'ADD' || command === 'IMPORT') {

				$(result).each(function (i, entity) {
					if (command === 'CREATE' && (entity.isPage || entity.isFolder || entity.isFile || entity.isImage || entity.isVideo || entity.isUser || entity.isGroup || entity.isWidget || entity.isResourceAccess)) {
						StructrModel.create(entity);
					} else {

						if (!entity.parent && shadowPage && entity.pageId === shadowPage.id) {

							entity = StructrModel.create(entity, null, false);
							var el;
							if (entity.isContent || entity.type === 'Template') {
								el = _Elements.appendContentElement(entity, components, true);
							} else {
								el = _Pages.appendElementElement(entity, components, true);
							}

							if (Structr.isExpanded(entity.id)) {
								_Entities.ensureExpanded(el);
							}

							var synced = entity.syncedNodes;

							if (synced && synced.length) {

								// Change icon
								$.each(entity.syncedNodes, function (i, id) {
									var el = Structr.node(id);
									if (el && el.length) {
										var icon = entity.isTemplate ? _Icons.icon_shared_template : (entity.isContent ? _Icons.active_content_icon : _Icons.comp_icon);
										el.children('.typeIcon').attr('class', 'typeIcon ' + _Icons.getFullSpriteClass(icon));
										_Entities.removeExpandIcon(el);
									}
								});

							}
						}
					}

					if (command === 'CREATE' && entity.isPage) {
						var tab = $('#show_' + entity.id, previews);
						setTimeout(function () {
							_Pages.activateTab(tab);
						}, 2000);
					} else if (command === 'CREATE' && (entity.isFile || entity.isImage || entity.isVideo)) {
						_Files.uploadFile(entity);
					}

					StructrModel.callCallback(data.callback, entity);

				});

				if (!LSWrapper.getItem(autoRefreshDisabledKey + activeTab)) {
					_Pages.reloadPreviews();
				}
			} else if (command === 'PROGRESS') {

				if (dialogMsg.is(':visible')) {
					var msgObj = JSON.parse(data.message);
					dialogMsg.html('<div class="infoBox info">' + msgObj.message + '</div>');
				}

			} else if (command === 'FINISHED') {

				StructrModel.callCallback(data.callback, data.data);

			} else if (command === 'AUTOCOMPLETE') {

				StructrModel.callCallback(data.callback, result);

			} else if (command === 'FIND_DUPLICATES') {

				StructrModel.callCallback(data.callback, result);

			} else if (command === 'SCHEMA_COMPILED') {

				_Schema.processSchemaRecompileNotification();

			} else {
				console.log('Received unknown command: ' + command);

				if (sessionValid === false) {
					_Logger.log(_LogType.WS[command], 'invalid session');
					user = null;
					clearMain();

					Structr.login();
				}
			}
		};

	} catch (exception) {
		_Logger.log(_LogType.WEBSOCKET, 'Error in connect(): ' + exception);
		if (ws) {
			ws.close();
			ws.length = 0;
		}
	}

}

function sendObj(obj, callback) {

	if (callback) {
		obj.callback = uuid.v4();
		StructrModel.callbacks[obj.callback] = callback;
	}

	var t = JSON.stringify(obj);

	if (!t) {
		_Logger.log(_LogType.WEBSOCKET, 'No text to send!');
		return false;
	}

	try {
		ws.send(t);
		_Logger.log(_LogType.WS[obj.command], 'Sent: ' + t);
	} catch (exception) {
		_Logger.log(_LogType.WEBSOCKET, 'Error in send(): ' + exception);
	}
	return true;
}

function send(text) {

	_Logger.log(_LogType.WEBSOCKET, 'Sending text: "' + text + '" - ws.readyState=' + ws.readyState);

	var obj = JSON.parse(text);
	return sendObj(obj);
}

function getAnchorFromUrl(url) {
	if (url) {
		var pos = url.lastIndexOf('#');
		if (pos > 0) {
			return url.substring(pos + 1, url.length);
		}
	}
	return null;
}

function utf8_to_b64(str) {
	return window.btoa(unescape(encodeURIComponent(str)));
}

function b64_to_utf8(str) {
	return decodeURIComponent(escape(window.atob(str)));
}