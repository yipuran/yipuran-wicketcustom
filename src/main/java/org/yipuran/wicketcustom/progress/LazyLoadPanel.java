package org.yipuran.wicketcustom.progress;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

/**
 * LazyLoadPanel.
 */
public abstract class LazyLoadPanel extends AjaxLazyLoadPanel{
	/**
	 * Constructor.
	 * @param id componemt ID
	 */
	public LazyLoadPanel(final String id){
		this(id, null);
	}
	/**
	 * Constructor.
	 * @param id componemt ID
	 * @param model Wicket Model
	 */
	public LazyLoadPanel(final String id, final IModel<?> model){
		super(id, model);
	}
	/**
	 * @param markupId
	 *            The components markupid.
	 * @return The component to show while the real component is being created.
	 */
	@Override
	public Component getLoadingComponent(final String markupId){
		StringBuilder sb = new StringBuilder();
		sb.append("<script type=\"text/javascript\">");
		sb.append("$(function(){ ");
		sb.append(" initProgressModal('#progress', { message: \"" + getMessage() + "\"});");
		sb.append("	displayProgressModal('#progress', true);");
		sb.append("});");
		sb.append("</script>");
		return new Label(markupId, sb.toString()).setEscapeModelStrings(false);
	}
	/**
	 * インジケータメッセージ.
	 * Loaddin... 以外のメッセージ表示する場合は、オーバーライドする。
	 * @return メッセージ
	 */
	public String getMessage(){
		return "Loading...";
	}

	@Override
	public void renderHead(IHeaderResponse response){
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(LazyLoadPanel.class, "progressmodal-1.2.min.css")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(LazyLoadPanel.class, "spin.min.js")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(LazyLoadPanel.class, "progressmodal-1.2.min.js")));
	}
}
