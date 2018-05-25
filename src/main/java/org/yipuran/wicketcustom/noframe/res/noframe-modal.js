/**
 * noframe-modal.js
 */

;(function (undefined) {
	'use strict';

	/**
	 * In case wicket-ajax.js is not yet loaded, create
	 * Noframe namespace and Noframe.Class.create.
	 */
	if (typeof(Noframe) === "undefined") {
		window.Noframe = {};
	}
	if (!Noframe.Class) {
		Noframe.Class = {
			create: function() {
				return function() {
					this.initialize.apply(this, arguments);
				};
			}
		};
	}
	if (!Noframe.Object) {
		Noframe.Object = { };
	}
	if (!Noframe.Object.extend) {
		Noframe.Object.extend = function(destination, source) {
			for (var property in source) {
				destination[property] = source[property];
			}
			return destination;
		};
	}
	/**
	 * Supporting code for getting mouse move and mouse up events from iframes.
	 * The problem when dragging a div with an iframe is that when the mouse cursor
	 * gets over an iframe, all mouse events are received by the iframe's document. (IE and FF)
	 *
	 * This code can recursively traverse all iframes in document and temporarily forward
	 * events from their documents to parent document.
	 */
	Noframe.Iframe = {
		/**
		 * Returns the horizontal position of given element (in pixels).
		 */
		findPosX: function(e) {
			if (e.offsetParent) {
				var c = 0;
				while (e) {
					c += e.offsetLeft;
					e = e.offsetParent;
				}
				return c;
			} else if (e.x) {
				return e.x;
			} else {
				return 0;
			}
		},
		/**
		 * Returns the vertical position of given element (in pixels).
		 */
		findPosY: function(e) {
			if (e.offsetParent) {
				var c = 0;
				while (e) {
					c += e.offsetTop;
					e = e.offsetParent;
				}
				return c;
			} else if (e.y) {
				return e.y;
			} else {
				return 0;
			}
		},
		/**
		 * Forwards the events from iframe to the parent document (works recursively).
		 * @param {Document} doc - document to which the events will be forwarded
		 * @param {HTMLElement} iframe - source iframe
		 * @param {Array} revertList - list to which altered iframes will be added
		 */
		forwardEvents: function(doc, iframe, revertList) {
			try {
				var idoc = iframe.contentWindow.document;
				idoc.old_onmousemove = idoc.onmousemove;
				idoc.onmousemove = function(evt) {
					if (!evt) {
						evt = iframe.contentWindow.event;
					}
					var e = {};
					var dx = 0;
					var dy = 0;
					if (Wicket.Browser.isIELessThan11() || Wicket.Browser.isGecko()) {
						dx = Noframe.Window.getScrollX();
						dy = Noframe.Window.getScrollY();
					}
					e.clientX = evt.clientX + Noframe.Iframe.findPosX(iframe) - dx;
					e.clientY = evt.clientY + Noframe.Iframe.findPosY(iframe) - dy;
					doc.onmousemove(e);
				};
				idoc.old_onmouseup = idoc.old_onmousemove;
				idoc.onmouseup = function(evt) {
					if (!evt) {
						evt = iframe.contentWindow.event;
					}
					var e = {};
					var dx = 0;
					var dy = 0;
					if (Wicket.Browser.isIELessThan11() || Wicket.Browser.isGecko()) {
						dx = Noframe.Window.getScrollX();
						dy = Noframe.Window.getScrollY();
					}
					e.clientX = evt.clientX + Noframe.Iframe.findPosX(iframe) - dx;
					e.clientY = evt.clientY + Noframe.Iframe.findPosY(iframe) - dy;
					doc.onmouseup(e);
				};
				revertList.push(iframe);
				Noframe.Iframe.documentFix(idoc, revertList);
			} catch (ignore) {
			}
		},
		/**
		 * Reverts the changes made to the given iframe.
		 * @param {HTMLElement} iframe
		 */
		revertForward: function(iframe) {
			var idoc = iframe.contentWindow.document;
			idoc.onmousemove = idoc.old_onmousemove;
			idoc.onmouseup = idoc.old_onmouseup;
			idoc.old_onmousemove = null;
			idoc.old_onmouseup = null;
		},
		/**
		 * Forward events from all iframes of the given document (recursive)
		 * @param {Document} doc - document to be fixed
		 * @param {Array} revertList - all affected iframes will be stored here
		 */
		documentFix: function(doc, revertList) {
			var iframes = doc.getElementsByTagName("iframe");
			for (var i = 0; i < iframes.length; ++i) {
				var iframe = iframes[i];
				if (iframe.tagName) {
					Noframe.Iframe.forwardEvents(doc, iframe, revertList);
				}
			}
		},
		/**
		 * Reverts the changes made to each iframe in the given array.
		 * @param {Array} revertList
		 */
		documentRevert: function(revertList) {
			for (var i = 0; i < revertList.length; ++i) {
				var iframe = revertList[i];
				Noframe.Iframe.revertForward(iframe);
			}
		}
	};
	/**
	 * Draggable (and optionally resizable) window that can either hold a div
	 * or an iframe.
	 */
	Noframe.Window = Noframe.Class.create();

	/**
	 * Creates a wicket window instance. The advantage of using this is
	 * that in case an iframe modal window is opened in an already displayed
	 * iframe modal window, the new window is created as a top-level window.
	 *
	 */
	Noframe.Window.create = function(settings) {
		var Win;
		// if it is an iframe window...
		if (typeof(settings.src) !== "undefined" && Wicket.Browser.isKHTML() === false) {
			// attempt to get class from parent
			try {
				Win = window.parent.Noframe.Window;
			} catch (ignore) {}
		}
		// no parent...
		if (typeof(Win) === "undefined") {
			Win = Noframe.Window;
		}
		// create and return instance
		return new Win(settings);
	};

	/**
	 * Returns the current top level window (null if none).
	 */
	Noframe.Window.get = function() {
		var win = null;
		if (typeof(Noframe.Window.current) !== "undefined") {
			win = Noframe.Window.current;
		}else{
			try{
				win = window.parent.Noframe.Window.current;
			}catch(ignore){}
		}
		return win;
	};
	/**
	 * Closes the current open window. This method is supposed to
	 * be called from inside the window (therefore it checks window.parent).
	 */
	Noframe.Window.close = function() {
		var win;
		try{
			win = window.parent.Noframe.Window;
		}catch(ignore){}
		if (win && win.current){
			// we can't call close directly, because it will delete our window,
			// so we will schedule it as timeout for parent's window
			window.parent.setTimeout(function() {
				win.current.close();
			}, 0);
		}
	};

	Noframe.Window.prototype = {
		/**
		 * Creates a new window instance.
		 * Note:
		 *   Width refers to the width of entire window (including frame).
		 *   Height refers to the height of user content.
		 *
		 * @param {Object} settings - map that contains window settings. the default
		 *                            values are below - together with description
		 */
		initialize: function(settings) {
			// override default settings with user settings
			this.settings = Noframe.Object.extend({
				minWidth: 200,  /* valid only if resizable */
				minHeight: 150, /* valid only if resizable */
				/* className: "w_blue", /* w_silver */
				/* width: 600,  /* initial width */
				/*height: 300, /* may be null for non-iframe, non-resizable window (automatic height) */
				resizable: false,
				widthUnit: "px", /* valid only if not resizable */
				heightUnit: "px", /* valid only if not resizable */
				src: null,     /* iframe src - this takes precedence over the "element" property */
				element: null, /* content element (for non-iframe window) */
				iframeName: null, /* name of the iframe */
				cookieId: null, /* id of position (and size if resizable) cookie */
				title: null, /* window title. if null and window content is iframe, title of iframe document will be used. */
				onCloseButton: Wicket.bind(function() {
					this.close();
					return false;
				}, this), /* called when close button is clicked */
				onClose: function() { }, /* called when window is closed */
				mask: "semi-transparent", /* or "transparent" */
				unloadConfirmation : true /* Display confirmation dialog if the user is about to leave a page (IE and FF) */
			}, settings || { });
		},

		/**
		 * Returns true if the window is iframe-based.
		 */
		isIframe: function() {
			return this.settings.src != null;
		},
		/**
		 * Creates the DOM elements of the window.
		 */
		createDOM: function() {
			var idWindow = this.newId();
			var idClassElement = this.newId();
			var idCaption = this.newId();
			var idFrame = this.newId();
			var idTop = this.newId();
			var idTopLeft = this.newId();
			var idTopRight = this.newId();
			var idLeft = this.newId();
			var idRight = this.newId();
			var idBottomLeft = this.newId();
			var idBottomRight = this.newId();
			var idBottom = this.newId();
			var idCaptionText = this.newId();
			var markup = Noframe.Window.getMarkup(idWindow, idFrame, this.isIframe());

			var element = document.createElement("div");
			element.id = idWindow;
			document.body.appendChild(element);
			Wicket.DOM.replace(element, markup);

			var _ = function(name) { return document.getElementById(name); };

			this.window = _(idWindow);
			this.classElement = _(idClassElement);
			this.caption = _(idCaption);
			this.content = _(idFrame);
			this.top = _(idTop);
			this.topLeft = _(idTopLeft);
			this.topRight = _(idTopRight);
			this.left = _(idLeft);
			this.right = _(idRight);
			this.bottomLeft = _(idBottomLeft);
			this.bottomRight = _(idBottomRight);
			this.bottom = _(idBottom);
			this.captionText = _(idCaptionText);

			if (Wicket.Browser.isIELessThan11()) {
				// IE stupid 3px bug - not fixed even in IE7 quirks!
				if (Noframe.Browser.isIEQuirks()) {
					this.topLeft.style.marginRight = "-3px";
					this.topRight.style.marginLeft = "-3px";
					this.bottomLeft.style.marginRight = "-3px";
					this.bottomRight.style.marginLeft = "-3px";
				}
			}

			// HACK - IE doesn't support position:fixed. Gecko does, however for a reason
			// we need to have background position: absolute, which makes the movement of
			// the window really jerky if the window stays position: fixed
			if (Wicket.Browser.isIELessThan11() || Wicket.Browser.isGecko()) {
				this.window.style.position = "absolute";
			}
		},

		/**
		 * Creates the new unique id for window element.
		 */
		newId: function() {
			return "_wicket_window_" + Noframe.Window.idCounter++;
		},

		/**
		 * Binds the handler to the drag event on given element.
		 */
		bind: function(element, handler) {
		},
		/**
		 * Unbinds the handler from a drag event on given element.
		 */
		unbind: function(element) {
		},

		/**
		 * Binds the event handlers to the elements.
		 */
		bindInit: function() {
		},
		/**
		 * Unbinds the event handlers.
		 */
		bindClean: function() {
			this.unbind(this.caption);
			this.unbind(this.bottomRight);
			this.unbind(this.bottomLeft);
			this.unbind(this.bottom);
			this.unbind(this.left);
			this.unbind(this.right);
			this.unbind(this.topLeft);
			this.unbind(this.topRight);
			this.unbind(this.top);
		},
		/**
		 * Returns the content document
		 */
		getContentDocument: function() {
			if (this.isIframe() === true) {
				return this.content.contentWindow.document;
			} else {
				return document;
			}
		},
		/**
		 * Places the window to the center of the viewport.
		 */
		center: function() {
			var scTop = 0;
			var scLeft = 0;

			if (Wicket.Browser.isIELessThan11() || Wicket.Browser.isGecko()) {
				scLeft = Noframe.Window.getScrollX();
				scTop = Noframe.Window.getScrollY();
			}
			var width = Noframe.Window.getViewportWidth();
			var height = Noframe.Window.getViewportHeight();
			var modalWidth = this.window.offsetWidth;
			var modalHeight = this.window.offsetHeight;
			if (modalWidth > width - 10) {
				this.window.style.width = (width - 10) + "px";
				modalWidth = this.window.offsetWidth;
			}
			if (modalHeight > height - 40) {
				this.content.style.height = (height - 40) + "px";
				modalHeight = this.window.offsetHeight;
			}
			var left = (width / 2) - (modalWidth / 2) + scLeft;
			var top = (height / 2) - (modalHeight / 2) + scTop;
			if (left < 0) {
				left = 0;
			}
			if (top < 0) {
				top = 0;
			}
			this.window.style.left = left + "px";
			this.window.style.top = top + "px";
		},
		cookieKey: "wicket-modal-window-positions",
		cookieExp: 31,
		findPositionString: function(remove) {
			var cookie = Noframe.Cookie.get(this.cookieKey);
			var entries = cookie != null ? cookie.split("|") : [];
			for (var i = 0; i < entries.length; ++i) {
				if (entries[i].indexOf(this.settings.cookieId + "::") === 0) {
					var string = entries[i];
					if (remove) {
						entries.splice(i, 1);
						Noframe.Cookie.set(this.cookieKey, entries.join("|"), this.cookieExp);
					}
					return string;
				}
			}
			return null;
		},

		/**
		 * Saves the position (and size if resizable) as a cookie.
		 */
		savePosition: function() {
			this.savePositionAs(this.window.style.left, this.window.style.top, this.window.style.width, this.content.style.height);
		},

		savePositionAs: function(x, y, width, height) {
			if (this.settings.cookieId) {
				this.findPositionString(true);
				var cookie = this.settings.cookieId;
				cookie += "::";
				cookie += x + ",";
				cookie += y + ",";
				cookie += width + ",";
				cookie += height;
				var rest = Noframe.Cookie.get(this.cookieKey);
				if (rest != null) {
					cookie += "|" + rest;
				}
				Noframe.Cookie.set(this.cookieKey, cookie, this.cookieExp);
			}
		},
		/**
		 * Restores the position (and size if resizable) from the cookie.
		 */
		loadPosition: function() {
			if (this.settings.cookieId) {
				var string = this.findPositionString(false);
				if (string != null) {
					var array = string.split("::");
					var positions = array[1].split(",");
					if (positions.length === 4) {
						this.window.style.left = positions[0];
						this.window.style.top = positions[1];
						this.window.style.width = positions[2];
						this.content.style.height = positions[3];
					}
				}
			}
		},
		/**
		 * Creates the mask accordingly to the settings.
		 */
		createMask: function() {
			if (this.settings.mask === "transparent") {
				this.mask = new Noframe.Window.Mask(true);
			} else if (this.settings.mask === "semi-transparent") {
				this.mask = new Noframe.Window.Mask(false);
			}
			if (typeof(this.mask) !== "undefined") {
				this.mask.show();
			}
		},

		/**
		 * Destroys the mask.
		 */
		destroyMask: function() {
			this.mask.hide();
			this.mask = null;
		},
		/**
		 * Loads the content
		 */
		load: function() {
			if (!this.settings.title) {
				this.update = window.setInterval(Noframe.bind(this.updateTitle, this), 100);
			}
			// opera seems to have problem accessing contentWindow here
			if (Noframe.Browser.isOpera()) {
				this.content.onload = Wicket.bind(function() {
					this.content.contentWindow.name = this.settings.iframeName;
				}, this);
			} else {
				this.content.contentWindow.name = this.settings.iframeName;
			}
			try	{
				this.content.contentWindow.location.replace(this.settings.src);
			}catch(ignore){
				this.content.src = this.settings.src;
			}
		},
		/**
		 * Shows the window.
		 */
		show: function() {
			// create the DOM elements
			this.createDOM();
			// set the class of window (blue or silver by default)
			// is it an iframe window?
			if (this.isIframe()) {
				// load the file
				this.load();
			}else{
				// it's an element content
				// is the element specified?
				if (this.settings.element == null) {
					throw "Either src or element must be set.";
				}
				// reparent the element
				this.oldParent = this.settings.element.parentNode;
				this.settings.element.parentNode.removeChild(this.settings.element);
				this.content.appendChild(this.settings.element);
				// set the overflow style so that scrollbars are shown when the element is bigger than window
				this.content.style.overflow="auto";
			}
			// bind the events
			this.bindInit();
			// if the title is specified set it
			if (this.settings.title != null) {
				this.captionText.innerHTML = this.settings.title;
				// http://www.w3.org/TR/wai-aria/states_and_properties#aria-labelledby
				//this.window.setAttribute('aria-labelledBy', this.settings.title);
			}

			// initial width and height
			this.window.style.width = this.settings.width + (this.settings.resizable ? "px" : this.settings.widthUnit);

			if (this.settings.height) {
				this.content.style.height = this.settings.height + (this.settings.resizable ? "px" : this.settings.heightUnit);
			}

			// center the window
			this.center();

			// load position from cookie
			this.loadPosition();

			var doShow = Wicket.bind(function() {
				this.adjustOpenWindowZIndexesOnShow();
				this.window.style.visibility="visible";

			}, this);

			this.adjustOpenWindowsStatusOnShow();

			// show the window
			if (false && Wicket.Browser.isGecko() && this.isIframe()) {
				// HACK
				// gecko flickers when showing the window
				// unless the showing is postponed a little
				window.setTimeout(function() { doShow(); }, 0);
			} else {
				doShow();
			}
			// if the content supports focus and blur it, which means
			// that the already focused element will lose it's focus
			if (this.content.focus) {
				this.content.focus();
				this.content.blur();
			}
			// preserve old unload hanler
			this.old_onunload = window.onunload;

			// new unload handler - close the window to prevent memory leaks in ie
			window.onunload = Wicket.bind(function() {
				this.close(true);
				if (this.old_onunload) {
					return this.old_onunload();
				}
			}, this);

			if (this.settings.unloadConfirmation) {
				Wicket.Event.add(window, 'beforeunload',this.onbeforeunload);
			}
			// create the mask that covers the background
			this.createMask();
		},

		onbeforeunload: function() {
			return "Reloading this page will cause the modal window to disappear.";
		},

		adjustOpenWindowZIndexesOnShow: function() {
			// if there is a previous window
			if (this.oldWindow) {
				// lower it's z-index so that it's moved under the mask
				this.oldWindow.window.style.zIndex = Noframe.Window.Mask.zIndex - 1;
			}
		},

		adjustOpenWindowsStatusOnShow: function() {
			// is there a window displayed already?
			if (Noframe.Window.current) {
				// save the reference to it
				this.oldWindow = Noframe.Window.current;
			}
			// keep reference to this window
			Noframe.Window.current = this;
		},

		/**
		 * Returns true if the window can be closed.
		 */
		canClose: function() {
			return true;
		},

		/**
		 * Prevent user from closing the window if there's another (nested) modal window in the iframe.
		 */
		canCloseInternal: function() {
			try {
				if (this.isIframe() === true) {
					var current = this.content.contentWindow.Noframe.Window.current;
					if (current) {
						window.alert('You can\'t close this modal window. Close the top-level modal window first.');
						return false;
					}
				}
			} catch (ignore) {}
			return true;
		},

		/**
		 * Closes the window.
		 * @param {Boolean} force - internal argument
		 */
		close: function(force) {
			// can user close the window?
			if (force !== true && (!this.canClose() || !this.canCloseInternal())) {
				return;
			}
			// if the update handler was set clean it
			if (typeof(this.update) !== "undefined") {
				window.clearInterval(this.update);
			}
			// clean event bindings
			this.bindClean();
			// hide elements
			this.window.style.display = "none";

			// if the window has a div content, the div is reparented to it's old parent
			if (typeof(this.oldParent) !== "undefined") {
				try {
					this.content.removeChild(this.settings.element);
					this.oldParent.appendChild(this.settings.element);
					this.oldParent = null;
				} catch (ignore) {}
			}
			// remove the elements from document
			this.window.parentNode.removeChild(this.window);
			// clean references to elements
			this.window = this.classElement = this.caption = this.bottomLeft = this.bottomRight = this.bottom =
			this.left = this.right = this.topLeft = this.topRight = this.top = this.captionText = null;
			// restore old unload handler
			window.onunload = this.old_onunload;
			this.old_onunload = null;
			Wicket.Event.remove(window, 'beforeunload',this.onbeforeunload);
			// hids and cleanup the mask
			this.destroyMask();
			if (force !== true) {
				// call onclose handler
				this.settings.onClose();
			}
			this.adjustOpenWindowsStatusAndZIndexesOnClose();

			if (Wicket.Browser.isIELessThan11()) {
				// There's a strange focus problem in IE that disables focus on entire page,
				// unless something focuses an input
				var e = document.createElement("input");
				var x = Noframe.Window.getScrollX();
				var y = Noframe.Window.getScrollY();
				e.style.position = "absolute";
				e.style.left = x + "px";
				e.style.top = y + "px";
				document.body.appendChild(e);
				e.focus();
				document.body.removeChild(e);
			}
		},

		adjustOpenWindowsStatusAndZIndexesOnClose: function() {
			// if there was a window shown before this one
			if (this.oldWindow != null) {
				// set the old as current
				Noframe.Window.current = this.oldWindow;
				// increase it's z-index so that it's moved above the mask
				Noframe.Window.current.window.style.zIndex = Noframe.Window.Mask.zIndex + 1;
				this.oldWindow = null;
			} else {
				// remove reference to the window
				Noframe.Window.current = null;
			}
		},
		/**
		 * Cleans the internal state of the window
		 */
		destroy: function() {
			this.settings = null;
		},
		/**
		 * If the window is Iframe, updates the title with iframe's document title.
		 */
		updateTitle: function() {
			try{
				if (this.content.contentWindow.document.title) {
					if (this.captionText.innerHTML !== this.content.contentWindow.document.title) {
						this.captionText.innerHTML = this.content.contentWindow.document.title;
						// http://www.w3.org/TR/wai-aria/states_and_properties#aria-labelledby
						this.window.setAttribute('aria-labelledBy', this.content.contentWindow.document.title);
						// konqueror doesn't refresh caption text properly
						if (Wicket.Browser.isKHTML()) {
							this.captionText.style.display = 'none';
							window.setTimeout(Wicket.bind(function() { this.captionText.style.display="block";}, this), 0);
						}
					}
				}
			}catch(ignore) {
				Noframe.Log.info(ignore);
			}
		},

		/**
		 * Called when dragging has started.
		 */
		onBegin: function(object) {
			if (this.isIframe() && (Wicket.Browser.isGecko() || Wicket.Browser.isIELessThan11() || Wicket.Browser.isSafari())) {
				this.revertList = [];
				Noframe.Iframe.documentFix(document, this.revertList);
			}
		},
		/**
		 * Called when dragging has ended.
		 */
		onEnd: function(object) {
			if (this.revertList) {
				Noframe.Iframe.documentRevert(this.revertList);
				this.revertList = null;
				if (Wicket.Browser.isKHTML() || this.content.style.visibility==='hidden') {
					this.content.style.visibility='hidden';
					window.setTimeout(Wicket.bind(function() { this.content.style.visibility='visible'; }, this),  0 );
				}
				this.revertList = null;
			}
			this.savePosition();
		},
		/**
		 * Called when window is moving (draggin the caption).
		 */
		onMove: function(object, deltaX, deltaY) {
			var w = this.window;
			this.left_ = parseInt(w.style.left, 10) + deltaX;
			this.top_ = parseInt(w.style.top, 10) + deltaY;
			if (this.left_ < 0) {
				this.left_ = 0;
			}
			if (this.top_ < 0) {
				this.top_ = 0;
			}
			w.style.left = this.left_ + "px";
			w.style.top = this.top_ + "px";
			this.moving();
		},
		/**
		 * Called when window is being moved
		 */
		moving: function() {
		},

		/**
		 * Called when window is resizing.
		 */
		resizing: function() {
		},
	};

	/**
	 * Counter for generating unique component ids.
	 */
	Noframe.Window.idCounter = 0;

	/**
	 * Returns the modal window markup with specified element identifiers.
	 */
	Noframe.Window.getMarkup = function(idWindow, idContent, isFrame) {
		var s = "<div class=\"noframe-modal\" id=\""+idWindow+"\" role=\"dialog\" style=\"top: 10px; left: 10px; width: 100px;\">"
				+ "<form style='background-color:transparent;padding:0px;margin:0px;border-width:0px;position:static'>"
				+ "<div class=\"w_content\">";
		if (isFrame){
			s+= "<iframe frameborder=\"0\" id=\""+idContent+"\" allowtransparency=\"false\" style=\"height: 200px\" class=\"wicket_modal\"></iframe>";
		}else{
			s+= "<div id='"+idContent+"' class='w_content_container'></div>";
		}
		s+= "</div>" + "</form></div>";
		return s;
	};

	/**
	 * Transparent or semi-transparent masks that prevents user from interacting
	 * with the portion of page behind a window.
	 */
	Noframe.Window.Mask = Noframe.Class.create();
	Noframe.Window.Mask.zIndex = 20000;
	Noframe.Window.Mask.prototype = {
		/**
		 * Creates the mask.
		 * Created mask is not visible immediately. You have to call <code>show()</code> to
		 * make it visible.
		 * @param {boolean} transparent - whether the mask should be transparent (true) or
		 *                                semi-transparent (false).
		 */
		initialize: function(transparent) {
			this.transparent = transparent;
		},
		/**
		 * Shows the mask.
		 */
		show: function() {

			// if the mask is not already shown...
			if (!Noframe.Window.Mask.element) {
				// create the mask element and add it to the document
				var e = document.createElement("div");
				document.body.appendChild(e);
				// set the proper css class name
				if (this.transparent) {
					e.className = "wicket-mask-transparent";
				} else {
					e.className = "wicket-mask-dark";
				}
				e.style.zIndex = Noframe.Window.Mask.zIndex;
				// HACK - KHTML doesn't support colors with alpha transparency
				// if the mask is not transparent we have to either
				// make the background image visible (setting color to transparent) - for KHTML
				// or make the background-image invisible (setting it to null) - for other browsers
				if (this.transparent === false) {
					if (Wicket.Browser.isKHTML() === false) {
						e.style.backgroundImage = "none";
					} else {
						e.style.backgroundColor = "transparent";
					}
				}
				// HACK - it really sucks that we have to set this to absolute even for gecko.
				// however background with position:fixed makes the text cursor in textfieds
				// in modal window disappear
				if (Wicket.Browser.isIELessThan11() || Wicket.Browser.isGecko()) {
					e.style.position = "absolute";
				}
				// set the element
				this.element = e;
				// preserver old handlers
				this.old_onscroll = window.onscroll;
				this.old_onresize = window.onresize;
				// set new handlers
				window.onscroll = Wicket.bind(this.onScrollResize, this);
				window.onresize = Wicket.bind(this.onScrollResize, this);
				// fix the mask position
				this.onScrollResize(true);
				// set a static reference to mask
				Noframe.Window.Mask.element = e;
			}else{
				// mask is already shown - don't hide it
				this.dontHide = true;
			}
			this.shown=true;
			this.focusDisabled=false;
			this.disableCoveredContent();
		},
		/**
		 * Hides the mask.
		 */
		hide: function(){
			// cancel any pending tasks
			this.cancelPendingTasks();
			// if the mask is visible and we can hide it
			if (typeof(Noframe.Window.Mask.element) !== "undefined" && typeof(this.dontHide) === "undefined") {
				// remove element from document
				document.body.removeChild(this.element);
				this.element = null;
				// restore old handlers
				window.onscroll = this.old_onscroll;
				window.onresize = this.old_onresize;
				Noframe.Window.Mask.element = null;
			}
			this.shown=false;
			this.reenableCoveredContent();
		},
		// disable user interaction for content that is covered by the mask
		disableCoveredContent: function() {
			var doc = document;
			var old = Noframe.Window.current.oldWindow;
			if (old) {
				doc = old.getContentDocument();
			}
			this.doDisable(doc, Noframe.Window.current);
		},
		tasks: [],
		startTask: function (fn, delay) {
			var taskId=setTimeout(Wicket.bind(function() { fn(); this.clearTask(taskId); }, this), delay);
			this.tasks.push(taskId);
		},
		clearTask: function (taskId) {
			var index=-1;
			for (var i=0;i<this.tasks.length;i++) {
				if (this.tasks[i] === taskId) {
					index=i;break;
				}
			}
			if (index>=0) {
				this.tasks.splice(index,1);
			}
		},
		cancelPendingTasks: function() {
			while (this.tasks.length>0) {
				var taskId=this.tasks.shift();
				clearTimeout(taskId);
			}
		},
		// disable user interaction for content that is covered by the mask inside the given document, taking into consideration that this modal window is or not in an iframe
		// and has the given content
		doDisable: function(doc, win) {
			this.startTask(Wicket.bind(function() {this.hideSelectBoxes(doc, win);}, this), 300);
			this.startTask(Wicket.bind(function() {this.disableTabs(doc, win);}, this), 400);
			this.startTask(Wicket.bind(function() {this.disableFocus(doc, win);}, this), 1000);
		},
		// reenable user interaction for content that was covered by the mask
		reenableCoveredContent: function() {
			// show old select boxes (ie only)
			this.showSelectBoxes();
			// restore tab order
			this.restoreTabs();
			// revert onfocus handlers
			this.enableFocus();
		},
		/**
		 * Used to update the position (ie) and size (ie, opera) of the mask.
		 */
		onScrollResize: function(dontChangePosition) {
			// if the iframe is not position:fixed fix it's position
			if (this.element.style.position === "absolute") {
				var w = Noframe.Window.getViewportWidth();
				var h = Noframe.Window.getViewportHeight();
				var scTop = 0;
				var scLeft = 0;
				scLeft = Noframe.Window.getScrollX();
				scTop = Noframe.Window.getScrollY();
				this.element.style.top = scTop + "px";
				this.element.style.left = scLeft + "px";
				if (document.all) { // opera or explorer
					this.element.style.width = w;
				}
				this.element.style.height = h;
			}
		},
		/**
		 * Returns true if 'element' is a child (anywhere in hierarchy) of 'parent'
		 */
		isParent: function(element, parent) {
			if (element.parentNode === parent) {
				return true;
			}
			if (typeof(element.parentNode) === "undefined" || element.parentNode === document.body) {
				return false;
			}
			return this.isParent(element.parentNode, parent);
		},
		/**
		 * For internet explorer hides the select boxes (because they
		 * have always bigger z-order than any other elements).
		 */
		hideSelectBoxes : function(doc, win) {
			if (!this.shown) {
				return;
			}
			if (Wicket.Browser.isIELessThan11()) {
				this.boxes = [];
				var selects = doc.getElementsByTagName("select");
				for (var i = 0; i < selects.length; i++) {
					var element = selects[i];
					// if this is not an iframe window and the select is child of window content,
					// don't hide it
					if (win.isIframe() === false && this.isParent(element, win.content)) {
						continue;
					}
					if (element.style.visibility !== "hidden") {
						element.style.visibility = "hidden";
						this.boxes.push(element);
					}
				}
			}
		},
		/**
		 * Shows the select boxes if they were hidden.
		 */
		showSelectBoxes: function() {
			if (typeof (this.boxes) !== "undefined") {
				for (var i = 0; i < this.boxes.length; ++i) {
					var element = this.boxes[i];
					element.style.visibility="visible";
				}
				this.boxes = null;
			}
		},
		/**
		 * Disable focus on element and all it's children.
		 */
		disableFocusElement: function(element, revertList, win) {
			if (win && win.window !== element) {
				revertList.push([element, element.onfocus]);
				element.onfocus = function() { element.blur(); };
				for (var i = 0; i < element.childNodes.length; ++i) {
					this.disableFocusElement(element.childNodes[i], revertList, win);
				}
			}
		},
		/**
		 * Disable focus on all elements in document
		 */
		disableFocus: function(doc, win) {
			if (!this.shown) {
				return;
			}
			// explorer doesn't need this, because for IE disableTabs() is called.
			// plus in IE this causes problems because it scrolls document		);
			if (Wicket.Browser.isIELessThan11() === false) {
				this.focusRevertList = [];
				var body = doc.getElementsByTagName("body")[0];
				for (var i = 0; i < body.childNodes.length; ++i) {
					this.disableFocusElement(body.childNodes[i], this.focusRevertList, win);
				}
			}
			this.focusDisabled=true;
		},
		/**
		 * Enables focus on all elements where the focus has been disabled.
		 */
		enableFocus: function() {
			if (this.focusDisabled === false) {
				return;
			}
			if (typeof(this.focusRevertList) !== "undefined") {
				for (var i = 0; i < this.focusRevertList.length; ++i) {
					var item = this.focusRevertList[i];
					item[0].onfocus = item[1];
				}
			}
			this.focusRevertList = null;
		},
		/**
		 * Disable tab indexes (ie).
		 */
		disableTabs: function (doc, win) {
			if (!this.shown) {
				return;
			}
			if (typeof (this.tabbableTags) === "undefined") {
				this.tabbableTags = ["A", "BUTTON", "TEXTAREA", "INPUT", "IFRAME", "SELECT"];
			}
			if (Wicket.Browser.isIELessThan11()) {
				this.disabledTabsRevertList = [];
				for (var j = 0; j < this.tabbableTags.length; j++) {
					var tagElements = doc.getElementsByTagName(this.tabbableTags[j]);
					for (var k = 0 ; k < tagElements.length; k++) {
						// if this is not an iframe window and the element is child of modal window,
						// don't disable tab on it
						if (win.isIframe() === true || this.isParent(tagElements[k], win.window) === false) {
							var element = tagElements[k];
							element.hiddenTabIndex = element.tabIndex;
							element.tabIndex="-1";
							this.disabledTabsRevertList.push(element);
						}
					}
				}
			}
		},
		/**
		 * Restore tab indexes if they were disabled.
		 */
		restoreTabs: function() {
			if (typeof (this.disabledTabsRevertList) !== "undefined" && this.disabledTabsRevertList !== null) {
				for (var i = 0; i < this.disabledTabsRevertList.length; ++i) {
					var element = this.disabledTabsRevertList[i];
					if (typeof(element.hiddenTabIndex) !== 'undefined') {
						element.tabIndex = element.hiddenTabIndex;
						try {
							delete element.hiddenTabIndex;
						} catch (e) {
							element.hiddenTabIndex = undefined;
						}
					}
				}
				this.disabledTabsRevertList = null;
			}
		}
	};
	/**
	 * Returns the height of visible area.
	 */
	Noframe.Window.getViewportHeight = function() {
		if (typeof(window.innerHeight) !== "undefined") {
			return window.innerHeight;
		}
		if (document.compatMode === 'CSS1Compat') {
			return document.documentElement.clientHeight;
		}
		if (document.body) {
			return document.body.clientHeight;
		}
		return undefined;
	};
	/**
	 * Returns the width of visible area.
	 */
	Noframe.Window.getViewportWidth =  function() {
		if (typeof(window.innerWidth) !== "undefined") {
			return window.innerWidth;
		}
		if (document.compatMode === 'CSS1Compat') {
			return document.documentElement.clientWidth;
		}
		if (document.body) {
			return document.body.clientWidth;
		}
		return undefined;
	};
	/**
	 * Returns the horizontal scroll offset
	 */
	Noframe.Window.getScrollX = function() {
		var iebody = (document.compatMode && document.compatMode !== "BackCompat") ? document.documentElement : document.body;
		return document.all? iebody.scrollLeft : window.pageXOffset;
	};
	/**
	 * Returns the vertical scroll offset
	 */
	Noframe.Window.getScrollY = function() {
		var iebody = (document.compatMode && document.compatMode !== "BackCompat") ? document.documentElement : document.body;
		return document.all? iebody.scrollTop : window.pageYOffset;
	};
	/**
	 * Convenience methods for getting and setting cookie values.
	 */
	Noframe.Cookie = {
		/**
		 * Returns the value for cookie of given name.
		 * @param {String} name - name of cookie
		 */
		get: function(name) {
			if (document.cookie.length > 0) {
				var start = document.cookie.indexOf (name + "=");
				if (start !== -1) {
					start = start + name.length + 1;
					var end = document.cookie.indexOf(";", start);
					if (end === -1) {
						end = document.cookie.length;
					}
					return window.unescape(document.cookie.substring(start,end));
				}
			} else {
				return null;
			}
		},
		/**
		 * Sets the value for cookie of given name.
		 * @param {Object} name - name of cookie
		 * @param {Object} value - new value
		 * @param {Object} expiredays - how long will the cookie be persisted
		 */
		set: function(name, value, expiredays) {
			var exdate = new Date();
			exdate.setDate(exdate.getDate() + expiredays);
			document.cookie = name + "=" + window.escape(value) + ((expiredays === null) ? "" : ";expires="+exdate);
		}
	};
})();
