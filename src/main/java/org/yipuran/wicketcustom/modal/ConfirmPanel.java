package org.yipuran.wicketcustom.modal;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.yipuran.wicketcustom.function.SerialThrowableConsumer;

/**
 * 確認メッセージ Panel.
 * AjaxRequestTarget で表示する確認メッセージ ModalWindow の Panel
 * <PRE>
 * ModalWindow window = new ModalWindow("confirmWindow");
 * window.setInitialWidth(420);
 * window.setInitialHeight(180);
 * window.setResizable(false);
 * queue(window);
 *
 * AjaxRequestTarget target;
 *
 * window.setContent(new ConfirmPanel(window.getContentId(), Model.of("削除してよろしいですか？"), "Yes", "No ",
 *    t->{
 *        // Yes.
 *    },
 *    t->{
 *        // No.
 *    }
 * ));
 * window.show(target);
 * </PRE>
 */
public class ConfirmPanel extends Panel{

	/**
	 * コンストラクタ.
	 * @param id Wicket-ID
	 * @param model 確認メッセージModel
	 * @param applyLabel left 左側ボタンラベル
	 * @param cancelLabel cancel 右側ボタンラベル
	 */
	public ConfirmPanel(String id, IModel<String> model, final String left, final String right, final SerialThrowableConsumer<AjaxRequestTarget> leftConsumer
			, final SerialThrowableConsumer<AjaxRequestTarget> rightConsumer){
		super(id, model);
		queue(new Label("message", Optional.ofNullable(model.getObject()).orElse("")));
		queue(new Form<Void>("form"));

		queue(new AjaxButton("left"){
			@Override
			public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag){
				replaceComponentTagBody(markupStream, openTag, left);
			}
			@Override
			protected void onSubmit(AjaxRequestTarget target){
				leftConsumer.accept(target);
				ModalWindow.closeCurrent(target);
			}
		});
		queue(new AjaxButton("right"){
			@Override
			public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag){
				replaceComponentTagBody(markupStream, openTag, right);
			}
			@Override
			protected void onSubmit(AjaxRequestTarget target){
				rightConsumer.accept(target);
				ModalWindow.closeCurrent(target);
			}
		});
	}
	@Override
	public void renderHead(IHeaderResponse response){
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(ConfirmPanel.class, "confirm-panel.css")));
	}
}
