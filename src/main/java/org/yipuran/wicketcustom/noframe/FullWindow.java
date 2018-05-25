package org.yipuran.wicketcustom.noframe;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.json.JSONFunction;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.string.AppendingStringBuffer;

import com.github.openjson.JSONObject;

/**
 * フルスクリーン Modal window component.
 */
public class FullWindow extends Panel{
	private static final long serialVersionUID = 1L;
	private static final ResourceReference JAVASCRIPT = new JavaScriptResourceReference(FullWindow.class, "res/full-modal.js");
	private static final ResourceReference CSS = new CssResourceReference(FullWindow.class,	"res/full-modal.css");

	/** the default id of the content component */
	public static final String CONTENT_ID = "content";

	/** True while the ModalWindows is showing */
	private boolean shown = false;

	/** empty container - used when no component is added */
	private WebMarkupContainer empty;

	private String cookieName;
	private boolean autoSize = false;
	private boolean unloadConfirmation = true;

	private PageCreator pageCreator = null;
	private CloseButtonCallback closeButtonCallback = null;
	private WindowClosedCallback windowClosedCallback = null;

	/**
	 * Interface for lazy page creation. The advantage of creating page using this interface over
	 * just passing a page instance is that page created in <code>{@link #createPage()}</code> will
	 * have the pagemap automatically set to the pagemap specified for
	 * <code>{@link FullWindow}</code>.
	 *
	 * @author Matej Knopp
	 */
	public interface PageCreator extends IClusterable{
		/**
		 * Creates a new instance of content page.
		 * @return new page instance
		 */
		Page createPage();
	}

	/**
	 * Callback for close button that contains a method that is invoked after the button has been
	 * clicked. If no callback instance is specified using
	 * <code>{@link FullWindow#setCloseButtonCallback(FullWindow.CloseButtonCallback)}</code>, no
	 * ajax request will be fired. Clicking the button will just close the window.
	 * @author Matej Knopp
	 */
	public interface CloseButtonCallback extends IClusterable{
		/**
		 * Methods invoked after the button has been clicked. The invocation is done using an ajax
		 * call, so <code>{@link org.apache.wicket.ajax.AjaxRequestTarget}</code> instance is
		 * available.
		 *
		 * @param target
		 *            <code>{@link org.apache.wicket.ajax.AjaxRequestTarget}</code> instance bound
		 *            with the ajax request.
		 *
		 * @return True if the window can be closed (will close the window), false otherwise
		 */
		boolean onCloseButtonClicked(AjaxRequestTarget target);
	}

	/**
	 * Callback called after the window has been closed. If no callback instance is specified using
	 * {@link FullWindow#setWindowClosedCallback(FullWindow.WindowClosedCallback)}, no ajax
	 * request will be fired.
	 *
	 * @author Matej Knopp
	 */
	public interface WindowClosedCallback extends IClusterable{
		/**
		 * Called after the window has been closed.
		 *
		 * @param target
		 *            <code>{@link org.apache.wicket.ajax.AjaxRequestTarget}</code> instance bound
		 *            with the ajax request.
		 */
		void onClose(AjaxRequestTarget target);
	}

	/**
	 * Creates a new modal window component.
	 * @param id Id of component
	 */
	public FullWindow(final String id)	{
		super(id);
		initialize();
	}

	/**
	 * Creates a new modal window component.
	 *
	 * @param id Id of component
	 * @param model Model
	 */
	public FullWindow(final String id, final IModel<?> model)	{
		super(id, model);
		initialize();
	}

	/**
	 * Initialize
	 */
	private void initialize()	{
		setVersioned(false);
		cookieName = null;
		add(empty = new WebMarkupContainer(getContentId()));
		add(newCloseButtonBehavior());
		add(new WindowClosedBehavior());
		// install a default callback that will force
		// WindowClosedBehavior to be executed
		setWindowClosedCallback((WindowClosedCallback) target->{
			// noop
		});
	}

	@Override
	public void renderHead(final IHeaderResponse response)	{
		super.renderHead(response);

		CoreLibrariesContributor.contributeAjax(getApplication(), response);
		response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT));

		ResourceReference cssResource = newCssResource();
		if (cssResource != null){
			response.render(CssHeaderItem.forReference(cssResource));
		}
	}
	/**
	 * Allows to override CSS contribution. Returning null means the CSS will be contributed via
	 * other sources, e.g. a global CSS resource.
	 *
	 * @return The CSS resource reference or null if CSS is contributed via other means.
	 * @see #setCssClassName(String)
	 */
	protected ResourceReference newCssResource(){
		return CSS;
	}

	/**
	 * Is this window currently showing.
	 *
	 * @return the shown
	 */
	public boolean isShown(){
		return shown;
	}


	/**
	 * Sets the <code>{@link PageCreator}</code> instance. The instance is only used when no custom
	 * component has been added to the dialog.
	 * @param creator  <code>{@link PageCreator}</code> instance
	 * @return this
	 */
	public FullWindow setPageCreator(final PageCreator creator){
		setContent(empty);
		pageCreator = creator;
		return this;
	}

	/**
	 * Sets the <code>{@link CloseButtonCallback}</code> instance.
	 * @param callback Callback instance
	 * @return this
	 */
	public FullWindow setCloseButtonCallback(final CloseButtonCallback callback)	{
		closeButtonCallback = callback;
		return this;
	}

	/**
	 * Sets the <code>@{link {@link WindowClosedCallback}</code> instance.
	 * @param callback  Callback instance
	 * @return this
	 */
	public FullWindow setWindowClosedCallback(final WindowClosedCallback callback)	{
		windowClosedCallback = callback;
		return this;
	}

	/**
	 * Shows the modal window.
	 * @param target  Request target associated with current ajax request.
	 */
	public void show(final IPartialPageRequestHandler target)	{
		if (shown == false){
			getContent().setVisible(true);
			target.add(this);
			target.appendJavaScript(getWindowOpenJavaScript());
			shown = true;
		}
	}

	/**
	 * Hides the modal window. This can be called from within the modal window, however, the modal
	 * window must have configured WindowClosedCallback. Otherwise use the
	 * {@link #close(IPartialPageRequestHandler)} method.
	 * @param target Request target associated with current ajax request.
	 */
	public static void closeCurrent(final IPartialPageRequestHandler target){
		target.appendJavaScript(getCloseJavacriptInternal());
	}

	/**
	 * Closes the modal window.
	 * @param target Request target associated with current ajax request.
	 */
	public void close(final IPartialPageRequestHandler target){
		getContent().setVisible(false);
		if (isCustomComponent()){
			target.add(getContent());
		}
		target.appendJavaScript(getCloseJavacript());
		shown = false;
	}
	/**
	 * Method that allows alternate script for showing the window.
	 * @return the script that actually shows the window.
	 */
	protected CharSequence getShowJavaScript(){
		return "window.setTimeout(function(){\n" + "  FullScreen.Window.create(settings).show();\n" + "}, 0);\n";
	}

	private static String getCloseJavacriptInternal(){
		return "var win;\n" //
			+ "try {\n" + "	win = window.parent.FullScreen.Window;\n"
			+ "} catch (ignore) {\n"
			+ "}\n"
			+ "if (typeof(win) == \"undefined\" || typeof(win.current) == \"undefined\") {\n"
			+ "  try {\n" + "     win = window.FullScreen.Window;\n"
			+ "  } catch (ignore) {\n"
			+ "  }\n"
			+ "}\n"
			+ "if (win && win.current) {\n"
			+ " var close = function(w) { w.setTimeout(function() {\n"
			+ "		win.current.close();\n"
			+ "	}, 0);  };\n"
			+ "	try { close(window.parent); } catch (ignore) { close(window); }\n" + "}";
	}

	/**
	 * Method that allows alternate script for closing the window.
	 *
	 * @return the script that actually closes the window.
	 */
	protected String getCloseJavacript(){
		return getCloseJavacriptInternal();
	}

	/**
	 * Returns the id of content component.
	 *
	 * <pre>
	 * FullWindow window = new FullWindow(parent, &quot;window&quot;);
	 * new MyPanel(window, window.getContentId());
	 * </pre>
	 *
	 * @return Id of content component.
	 */
	public String getContentId()	{
		return CONTENT_ID;
	}

	/**
	 * Sets a flag whether to ask the user before leaving the page.
	 *
	 * @param unloadConfirmation   a flag whether to ask the user before leaving the page
	 * @return {@code this} instance, for chaining
	 */
	public FullWindow showUnloadConfirmation(final boolean unloadConfirmation){
		this.unloadConfirmation = unloadConfirmation;
		return this;
	}

	/**
	 * Returns whether the user should be asked before leaving the page.
	 *
	 * @return {@code true} if the user should be asked if the last action causes leaving the page,
	 *         {@code false} otherwise
	 */
	public boolean showUnloadConfirmation(){
		return unloadConfirmation;
	}


	/**
	 * Returns the CSS unit for initial window width.
	 *
	 * @return CSS unit for initial window width.
	 */
	public String getWidthUnit(){
		return "px";
	}

	/**
	 * Retrns the CSS unit for initial window height.
	 *
	 * @return CSS unit for initial window height.
	 */
	public String getHeightUnit(){
		return "px";
	}

	/**
	 * Sets the name of the cookie that is used to remember window position (and size if the window
	 * is resizable).
	 *
	 * @param cookieName
	 *            Name of the cookie
	 * @return this
	 */
	public FullWindow setCookieName(final String cookieName){
		if ((cookieName != null) && (cookieName.contains(",") || cookieName.contains("|"))){
			throw new IllegalArgumentException("Cookie name may not contain ',' or '|' characters.");
		}
		this.cookieName = cookieName;
		return this;
	}

	/**
	 * Returns the name of cookie that is used to remember window position (and size if the window
	 * is resizable).
	 *
	 * @return Name of the cookie
	 */
	public String getCookieName(){
		return cookieName;
	}

	/**
	 * Creates the page.
	 *
	 * @return Page instance or null if page couldn't be created.
	 */
	private Page createPage(){
		if (pageCreator == null)
		{
			return null;
		}else{
			return pageCreator.createPage();
		}
	}

	/**
	 * org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender(){
		shown = makeContentVisible();

		getContent().setOutputMarkupId(true);
		getContent().setVisible(shown);

		super.onBeforeRender();
	}

	/**
	 * You may subclass this method in case you don't want to show up the window on normal page
	 * refresh.
	 *
	 * @return true, if the window shall be shown
	 */
	protected boolean makeContentVisible(){
		// if user is refreshing whole page, the window will not be shown
		if (getWebRequest().isAjax() == false){
			return false;
		}else	{
			return shown;
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.panel.Panel#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag){
		super.onComponentTag(tag);
		tag.put("style", "display:none");
	}

	/**
	 * Returns a content component. In case user haven't specified any content component, it returns
	 * an empty WebMarkupContainer.
	 *
	 * @return Content component
	 */
	protected final Component getContent(){
		return get(getContentId());
	}

	/**
	 * Returns true if user has added own component to the window.
	 *
	 * @return True if user has added own component to the window, false otherwise.
	 */
	protected boolean isCustomComponent(){
		return getContent() != empty;
	}

	/**
	 * org.apache.wicket.MarkupContainer#remove(org.apache.wicket.Component)
	 */
	@Override
	public FullWindow remove(final Component component){
		super.remove(component);
		if (component.getId().equals(getContentId())){
			add(empty = new WebMarkupContainer(getContentId()));
		}
		return this;
	}

	/**
	 * Sets the content of the modal window.
	 *
	 * @param component
	 * @return this;
	 */
	public FullWindow setContent(final Component component){
		if (component.getId().equals(getContentId()) == false){
			throw new WicketRuntimeException("Modal window content id is wrong. Component ID:" +
				component.getId() + "; content ID: " + getContentId());
		}else if (component instanceof AbstractRepeater)	{
			throw new WicketRuntimeException(
				"A repeater component cannot be used as the content of a modal window, please use repeater's parent");
		}

		component.setOutputMarkupPlaceholderTag(true);
		component.setVisible(false);
		replace(component);
		shown = false;
		pageCreator = null;
		return this;
	}

	/**
	 * @author Matej Knopp
	 */
	private class WindowClosedBehavior extends AbstractDefaultAjaxBehavior{
		private static final long serialVersionUID = 1L;
		@Override
		protected void respond(final AjaxRequestTarget target){
			shown = false;
			if (windowClosedCallback != null){
				windowClosedCallback.onClose(target);
			}
		}
	}

	/**
	 * @author Matej Knopp
	 */
	protected class CloseButtonBehavior extends AbstractDefaultAjaxBehavior	{
		private static final long serialVersionUID = 1L;
		public CloseButtonBehavior(){
		}
		@Override
		protected final void respond(final AjaxRequestTarget target){
			if ((closeButtonCallback == null) ||
				(closeButtonCallback.onCloseButtonClicked(target)))
			{
				close(target);
			}
		}
	}

	/**
	 * Returns the markup id of the component.
	 *
	 * @return component id
	 */
	private String getContentMarkupId(){
		return getContent().getMarkupId();
	}

	/**
	 * Returns the javascript used to open the window. Subclass
	 * {@link #postProcessSettings(AppendingStringBuffer)} to modify the JavaScript if needed.
	 *
	 * See WICKET-12
	 *
	 * @return javascript that opens the window
	 */
	protected final String getWindowOpenJavaScript(){
		JSONObject settings = new JSONObject();
		settings.put("minWidth", (Object)null);
		settings.put("minHeight", (Object)null);
		settings.put("width", (Object)null);
		settings.put("height", (Object)null);
		settings.put("resizable", false);
		settings.put("widthUnit", "px");
		settings.put("heightUnit", "px");
		if (isCustomComponent() == false){
			Page page = createPage();
			if (page == null){
				throw new WicketRuntimeException("Error creating page for modal dialog.");
			}
			CharSequence pageUrl;
			RequestCycle requestCycle = RequestCycle.get();

			page.getSession().getPageManager().touchPage(page);
			if (page.isPageStateless()){
				pageUrl = requestCycle.urlFor(page.getClass(), page.getPageParameters());
			}else	{
				IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(page));
				pageUrl = requestCycle.urlFor(handler);
			}
			settings.put("src", pageUrl);
		}else{
			settings.put("element", new JSONFunction("document.getElementById(\"" + getContentMarkupId() + "\")"));
		}

		if (getCookieName() != null){
			settings.put("cookieId", getCookieName());
		}
		settings.put("autoSize", false);
		settings.put("unloadConfirmation", showUnloadConfirmation());

		// set true if we set a windowclosedcallback
		boolean haveCloseCallback = false;

		// in case user is interested in window close callback or we have a pagemap to clean attach
		// notification request
		if (windowClosedCallback != null){
			WindowClosedBehavior behavior = getBehaviors(WindowClosedBehavior.class).get(0);
			settings.put("onClose", new JSONFunction("function() { " + behavior.getCallbackScript() + " }"));
			haveCloseCallback = true;
		}

		// in case we didn't set windowclosecallback, we need at least callback on close button, to
		// close window property (thus cleaning the shown flag)
		if ((closeButtonCallback != null) || (haveCloseCallback == false)){
			CloseButtonBehavior behavior = getBehaviors(CloseButtonBehavior.class).get(0);
			settings.put("onCloseButton", new JSONFunction("function() { " + behavior.getCallbackScript() + "; return false; }"));
		}

		postProcessSettings(settings);

		AppendingStringBuffer buffer = new AppendingStringBuffer(500);
		buffer.append("var settings = ");
		buffer.append(settings.toString());
		buffer.append(";");

		buffer.append(getShowJavaScript());
		return buffer.toString();
	}

	/**
	 * Method that allows tweaking the settings
	 *
	 * @param settings
	 * @return settings json
	 */
	protected void postProcessSettings(JSONObject settings){
	}

	/**
	 * Detach the 'title' model
	 *
	 * org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach(){
		super.onDetach();
	}

	/**
	 * Sets whether window size will be automatically adjusted on opening to fit content's width and
	 * height. <span style="text-decoration: underline">Doesn't work on IE 6.</span>
	 *
	 * @param autoSize
	 *            Whether window size will be automatically adjusted
	 * @return this
	 */
	public FullWindow setAutoSize(final boolean autoSize){
		this.autoSize = autoSize;
		return this;
	}

	/**
	 * Returns whether window will be opened in autosize mode.
	 *
	 * @return True if the window will be opened open in autosize mode, false otherwise
	 */
	public boolean isAutoSize(){
		return autoSize;
	}

	/**
	 * Gives the possibility to provide custom
	 * {@link org.apache.wicket.ajax.attributes.IAjaxCallListener}
	 *
	 * @return the behavior that should be used for the window close button
	 */
	protected CloseButtonBehavior newCloseButtonBehavior(){
		return new CloseButtonBehavior();
	}
}
