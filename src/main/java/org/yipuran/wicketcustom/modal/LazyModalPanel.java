package org.yipuran.wicketcustom.modal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.yipuran.wicketcustom.function.SerialThrowableConsumer;

/**
 * for Lazy Process ModalWindow Panel.
 * <PRE>
 * 遅い処理を実行する時に表示する ModalWindow 用Panel
 * 処理後自動でモーダルを閉じる場合と結果メッセージを表示する２通りが用意されている。
 *
 *
 * (記述例)
 *
 * 処理後自動でモーダルを閉じる場合
 *
 * final ModalWindow window = new ModalWindow("lazy_window").setResizable(true).setAutoSize(true);
 * queue(window);
 *
 * queue(new Button("link").add(AjaxEventBehavior.onEvent("click", t->{
 *    window.setContent(new LazyModalPanel(window.getContentId(), Model.of("処理中..."), u->{
 *
 *         // 時間がかかる処理
 *
 *    }, x->{
 *
 *         // 例外捕捉
 *
 *    }));
 *    window.show(t);
 * })));
 *
 *
 * 終了後、メッセージを表示する場合、（CLOSEボタン style指定あり）
 *
 * queue(new Button("link").add(AjaxEventBehavior.onEvent("click", t->{
 *    window.setContent(new LazyModalPanel(window.getContentId(), Model.of("処理中...")
 *       , "OK"
 *       , Arrays.asList("btn", "btn-primary")
 *       , u->{
 *
 *          //  時間がかかる処理
 *
 *          return "正常終了<br/>メッセージ";
 *    }, x->{
 *
 *          // 例外捕捉
 *
 *          return "異常終了";
 *    }));
 *    window.show(t);
 * })));
 *
 * </PRE>
 */
public class LazyModalPanel extends Panel{
	/**
	 * コンストラクタ（処理後、自動で閉じる）.
	 * @param id ModalWindow wicket-ID
	 * @param model Panle表示用メッセージ
	 * @param consumer 目的処理を実行する SerializableConsumer 引数は、AjaxRequestTarget
	 * @param oncatch 例外捕捉した SerializableConsumer 引数は発生した Exception
	 */
	public LazyModalPanel(String id, IModel<String> model, SerializableConsumer<AjaxRequestTarget> consumer, SerializableConsumer<Exception> oncatch){
		super(id, model);
		queue(new Form<Void>("lazy_modal_form"));
		queue(new Label("closeLabel", ""));
		queue(new Label("message", Optional.ofNullable(model.getObject()).orElse("")));
		queue(new Button("lazy_modal_start").add(AjaxEventBehavior.onEvent("click", SerialThrowableConsumer.of(t->{
			consumer.accept(t);
			ModalWindow.closeCurrent(t);
		},(t, x)->{
			oncatch.accept(x);
			ModalWindow.closeCurrent(t);
		}))));
		queue(new Button("lazy_modal_close"));
	}
	/**
	 * コンストラクタ（終了後結果表示する場合）.
	 * @param id Wicket-ID
	 * @param model 処理中メッセージ
	 * @param closeLabel CLOSEボタンのラベル。.setEscapeModelStrings(false)は実行される
	 * @param function 目的の処理を実行して正常終了のメッセージを返す Throwable な Function
	 *                  例外発生すると oncatch指定の Function が実行される
	 * @param oncatch 目的の処理で例外を捕捉した時に実行する Function
	 *                 表示する異常終了のメッセージを返す Function.
	 */
	public LazyModalPanel(String id, IModel<String> model
			, String closeLabel
			, SerializableFunction<AjaxRequestTarget, String> function
			, SerializableFunction<Exception, String> oncatch){
		super(id, model);
		queue(new Form<Void>("lazy_modal_form"));
		final Label mesageLabel = new Label("message", Optional.ofNullable(model.getObject()).orElse(""));
		mesageLabel.setEscapeModelStrings(false);
		mesageLabel.setOutputMarkupId(true);
		queue(mesageLabel);
		queue(new Label("closeLabel", closeLabel).setEscapeModelStrings(false));
		queue(new Button("lazy_modal_start")
		.add(AjaxEventBehavior.onEvent("click", SerialThrowableConsumer.of(t->{
			String s = function.apply(t);
			mesageLabel.setDefaultModelObject(Optional.ofNullable(s).orElse(null));
			t.appendJavaScript("convergementLazy();");
			t.add(mesageLabel);
		},(t, x)->{
			String s = oncatch.apply(x);
			mesageLabel.setDefaultModelObject(Optional.ofNullable(s).orElse(null));
			t.appendJavaScript("convergementLazy();");
			t.add(mesageLabel);
		}))));
		queue(new Button("lazy_modal_close").add(AjaxEventBehavior.onEvent("click", t->ModalWindow.closeCurrent(t))));
	}

	/**
	 * コンストラクタ（終了後結果表示する場合）CLOSEボタン style指定あり
	 * @param id Wicket-ID
	 * @param model 処理中メッセージ
	 * @param closeLabel CLOSEボタンのラベル。.setEscapeModelStrings(false)は実行される
	 * @param stylelist CLOSEボタンに付けるclass属性、
	 * @param function 目的の処理を実行して正常終了のメッセージを返す Throwable な Function
	 *                  例外発生すると oncatch指定の Function が実行される
	 * @param oncatch 目的の処理で例外を捕捉した時に実行する Function
	 *                 表示する異常終了のメッセージを返す Function
	 */
	public LazyModalPanel(String id, IModel<String> model
			, String closeLabel
			, List<String> stylelist
			, SerializableFunction<AjaxRequestTarget, String> function
			, SerializableFunction<Exception, String> oncatch){
		super(id, model);
		queue(new Form<Void>("lazy_modal_form"));
		final Label mesageLabel = new Label("message", Optional.ofNullable(model.getObject()).orElse(""));
		mesageLabel.setEscapeModelStrings(false);
		mesageLabel.setOutputMarkupId(true);
		queue(mesageLabel);
		queue(new Label("closeLabel", closeLabel).setEscapeModelStrings(false));
		queue(new Button("lazy_modal_start")
		.add(AjaxEventBehavior.onEvent("click", SerialThrowableConsumer.of(t->{
			String s = function.apply(t);
			mesageLabel.setDefaultModelObject(Optional.ofNullable(s).orElse(null));
			t.appendJavaScript("convergementLazy();");
			t.add(mesageLabel);
		},(t, x)->{
			String s = oncatch.apply(x);
			mesageLabel.setDefaultModelObject(Optional.ofNullable(s).orElse(null));
			t.appendJavaScript("convergementLazy();");
			t.add(mesageLabel);
		}))));
		queue(new Button("lazy_modal_close"){
			@Override
			protected void onComponentTag(ComponentTag tag){
				super.onComponentTag(tag);
				Optional.ofNullable(stylelist).filter(li->li.size() > 0).ifPresent(li->{
					tag.put("class", li.stream().collect(Collectors.joining(" ")));
				});
			}
		}.add(AjaxEventBehavior.onEvent("click", t->ModalWindow.closeCurrent(t))));
	}


	@Override
	protected void onAfterRender(){
		super.onAfterRender();
		JavaScriptUtils.writeJavaScript(getResponse(), "setTimeout('sizefitLazyModal();$(\"#lazy_modal_start\").trigger(\"click\");', 100);" );
	}
	@Override
	public void renderHead(IHeaderResponse response){
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(LazyModalPanel.class, "lazymodal.css")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(LazyModalPanel.class, "spin.min.js")));
	}
}
