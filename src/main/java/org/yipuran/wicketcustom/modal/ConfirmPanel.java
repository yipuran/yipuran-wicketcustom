package org.yipuran.wicketcustom.modal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.util.string.JavaScriptUtils;
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
 * <PRE>
 * AjaxRequestTarget で表示する確認メッセージ ModalWindow の Panel
 * 処理の実行を差し込むのは、ボタンをクリックした時に SerializableConsumer で指定する処理を実行して
 * モーダルが閉じられる。
 * 表示するメッセージ Label は、setEscapeModelStrings(false) が実行される
 *
 *
 *  （記述例）
 * ModalWindow window = new ModalWindow("confirmWindow").setResizable(true).setAutoSize(true);
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
 *
 * ボタンに class属性を付与する場合は、リストで属性値を指定する。
 *
 * window.setContent(new ConfirmPanel(window.getContentId(), Model.of("削除してよろしいですか？"), "Yes", "No ", Arrays.asList("btn", "btn-primary"),
 *    t->{
 *        // Yes.
 *    },
 *    t->{
 *        // No.
 *    }
 * ));
 *
 * </PRE>
 */
public class ConfirmPanel extends Panel{

	/**
	 * コンストラクタ.
	 * @param id Wicket-ID
	 * @param model 確認メッセージModel
	 * @param applyLabel left 左側ボタンラベル
	 * @param cancelLabel cancel 右側ボタンラベル
	 * @param leftConsumer 左側ボタン実行時に実行する SerialThrowableConsumer
	 * @param rightConsumer 右側ボタン実行時に実行する SerialThrowableConsumer
	 */
	public ConfirmPanel(String id, IModel<String> model, final String left, final String right, final SerialThrowableConsumer<AjaxRequestTarget> leftConsumer
			, final SerialThrowableConsumer<AjaxRequestTarget> rightConsumer){
		super(id, model);
		queue(new Label("message", Optional.ofNullable(model.getObject()).orElse("")).setEscapeModelStrings(false));
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
	/**
	 * コンストラクタ（ボタンに style 属性付ける場合）.
	 * @param id Wicket-ID
	 * @param model 確認メッセージModel
	 * @param applyLabel left 左側ボタンラベル
	 * @param cancelLabel cancel 右側ボタンラベル
	 * @param stylelist ボタンに付けるclass属性、
	 * @param leftConsumer 左側ボタン実行時に実行する SerialThrowableConsumer
	 * @param rightConsumer 右側ボタン実行時に実行する SerialThrowableConsumer
	 */
	public ConfirmPanel(String id, IModel<String> model, final String left, final String right
			, List<String> stylelist
			, final SerialThrowableConsumer<AjaxRequestTarget> leftConsumer
			, final SerialThrowableConsumer<AjaxRequestTarget> rightConsumer){
		super(id, model);
		queue(new Label("message", Optional.ofNullable(model.getObject()).orElse("")).setEscapeModelStrings(false));
		queue(new Form<Void>("form"));

		queue(new AjaxButton("left"){
			@Override
			protected void onComponentTag(ComponentTag tag){
				super.onComponentTag(tag);
				Optional.ofNullable(stylelist).filter(li->li.size() > 0).ifPresent(li->{
					tag.put("class", li.stream().collect(Collectors.joining(" ")));
				});
			}
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
			protected void onComponentTag(ComponentTag tag){
				super.onComponentTag(tag);
				Optional.ofNullable(stylelist).filter(li->li.size() > 0).ifPresent(li->{
					tag.put("class", li.stream().collect(Collectors.joining(" ")));
				});
			}
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
	protected void onAfterRender(){
		super.onAfterRender();
		JavaScriptUtils.writeJavaScript(getResponse(), "setTimeout('sizefitConfirmMessageModal();', 100);" );
	}

	@Override
	public void renderHead(IHeaderResponse response){
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(ConfirmPanel.class, "confirm-panel.css")));
	}
}
