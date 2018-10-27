package org.yipuran.wicketcustom.modal;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
 * AjaxRequestTarget で表示する確認メッセージ ModalWindow の Panel
 * <PRE>
 * ModalWindow window = new ModalWindow("messageWindow");
 * window.setInitialWidth(420);
 * window.setInitialHeight(180);
 * window.setResizable(false);
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
 * </PRE>
 * メッセージ ModalWindow のサイズを自動で調整したい場合は、MessagePanel 生成時、以下のように new MessagePanel(..){}に追記で
 * サイズ を Javascript で調整する関数を実行する方法がある
 * <PRE>
 * window.setContent(new MessagePanel(window.getContentId(), Model.of("削除しました"), "OK",
 *    t->{
 *        // 処理
 *    }
 * ){
 *      ＠Override
 *      protected void onAfterRender(){
 *         super.onAfterRender();
 *         JavaScriptUtils.writeJavaScript(getResponse(), "setTimeout('sizefit();', 100);" );
 *      }
 * });
 *
 * JavaScript  sizefit メソッド
 *     var sizefit = function(){
 *        $('.w_content_container').css("height", $('.modal-panel-content ul').outerHeight(true) + "px" );
 *        $('.wicket-modal').css("width", $('.modal-panel-content ul').outerWidth(true) + 22 + "px" );
 *     };
 *
 * </PRE>
 */
public class MessagePanel extends Panel{

	/**
	 * コンストラクタ.
	 * @param id content Wicket-ID
	 * @param model メッセージ格納 Model
	 * @param SerializableConsumer モーダルコンテンツの CLOSEボタン実行時に実行する Consumer
	 */
	public MessagePanel(String id, IModel<String> model, String closeLabel, SerializableConsumer<AjaxRequestTarget> consumer){
		super(id, model);
		queue(new Form<Void>("form"));
		queue(new Label("message", Optional.ofNullable(model.getObject()).orElse("")));
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
	@Override
	public void renderHead(IHeaderResponse response){
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(MessagePanel.class, "message-panel.css")));
	}
}
