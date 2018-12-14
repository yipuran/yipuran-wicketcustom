package org.yipuran.wicketcustom.modal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.yipuran.wicketcustom.function.SerialThrowableConsumer;

/**
 * メッセージ Panel.
 * <PRE>
 * AjaxRequestTarget で表示する確認メッセージ ModalWindow の Panel
 * 処理の実行を差し込むのは、クローズボタンをクリックした時に SerializableConsumer で指定する処理を実行して
 * モーダルが閉じられる。
 * 表示するメッセージ Label は、setEscapeModelStrings(false) が実行される
 *
 * （記述例）
 * ModalWindow window = new ModalWindow("messageWindow");
 * window.setResizable(true).setAutoSize(true);
 * queue(window);
 *
 * AjaxRequestTarget target;
 *
 * window.setContent(new MessagePanel(window.getContentId(), Model.of("削除しました"), "OK",
 *    t->{
 *        // 処理
 *    }
 * ));
 * window.show(target);
 *
 * CLOSEボタンに class属性を付与する場合は、リストで属性値を指定する。
 *
 * window.setContent(new MessagePanel(window.getContentId(), Model.of("削除しました"), "OK", Arrays.asList("btn", "btn-primary"),
 *    t->{
 *        // 処理
 *    }
 * ));
 *
 * </PRE>
 */
public class MessagePanel extends Panel{

	/**
	 * コンストラクタ.
	 * @param id content Wicket-ID
	 * @param model メッセージ格納 Model
	 * @param closeLabel クローズボタンのラベル文字列
	 * @param consumer SerializableConsumer モーダルコンテンツの CLOSEボタン実行時に実行する Consumer
	 */
	public MessagePanel(String id, IModel<String> model, String closeLabel, SerializableConsumer<AjaxRequestTarget> consumer){
		super(id, model);
		queue(new Form<Void>("form"));
		queue(new Label("message", Optional.ofNullable(model.getObject()).orElse("")).setEscapeModelStrings(false));
		queue(new Button("close"){
			@Override
			public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag){
				replaceComponentTagBody(markupStream, openTag, closeLabel);
			}
		}.add(AjaxEventBehavior.onEvent("click", SerialThrowableConsumer.of(t->{
			consumer.accept(t);
			ModalWindow.closeCurrent(t);
		}))));
	}
	/**
	 * コンストラクタ（クローズボタンに style 属性付ける場合）.
	 * @param id content Wicket-ID
	 * @param model メッセージ格納 Model
	 * @param closeLabel クローズボタンのラベル文字列
	 * @param stylelist CLOSEボタンに付けるclass属性、
	 * @param consumer SerializableConsumer モーダルコンテンツの CLOSEボタン実行時に実行する Consumer
	 */
	public MessagePanel(String id, IModel<String> model, String closeLabel, List<String> stylelist, SerializableConsumer<AjaxRequestTarget> consumer){
		super(id, model);
		queue(new Form<Void>("form"));
		queue(new Label("message", Optional.ofNullable(model.getObject()).orElse("")).setEscapeModelStrings(false));
		queue(new Button("close"){
			@Override
			protected void onComponentTag(ComponentTag tag){
				super.onComponentTag(tag);
				Optional.ofNullable(stylelist).filter(li->li.size() > 0).ifPresent(li->{
					tag.put("class", li.stream().collect(Collectors.joining(" ")));
				});
			}
			@Override
			public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag){
				replaceComponentTagBody(markupStream, openTag, closeLabel);
			}
		}.add(AjaxEventBehavior.onEvent("click", SerialThrowableConsumer.of(t->{
			consumer.accept(t);
			ModalWindow.closeCurrent(t);
		}))));
	}

	@Override
	protected void onAfterRender(){
		super.onAfterRender();
		JavaScriptUtils.writeJavaScript(getResponse(), "setTimeout('sizefitMessageModal();', 100);" );
	}

	@Override
	public void renderHead(IHeaderResponse response){
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(MessagePanel.class, "message-panel.css")));
	}
}
