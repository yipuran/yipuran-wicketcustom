package org.yipuran.wicketcustom.modal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * 確認メッセージモーダルウィンドウ.
 * 呼び出しPage は、ModalCallbacks を実装すること。
 * <PRE>
 * apply  で、((ModalCallbacks)getPage()).modalCallback(true);  が実行される。
 * cancel で、((ModalCallbacks)getPage()).modalCallback(false); が実行される。
 *
 * ModalWindow confirmWindow = new ModalWindow("confirmWindow");
 * confirmWindow.setInitialWidth(360);
 * confirmWindow.setInitialHeight(120);
 * confirmWindow.setResizable(false);
 * confirmWindow.setWindowClosedCallback(new WindowClosedCallback(){
 * 	@Override
 * 	public void onClose(AjaxRequestTarget target){
 * 		if (getModalCloseActionReason().equals(ModalCloseActionReason.APPLY)){
 * 			// Apply
 *       }
 * 	}
 * });
 * queue(confirmWindow);
 *
 * confirmWindow.setContent(new ConfirmDialog(deleteConfirmWindow.getContentId(), new Model<>("削除してよろしいですか？"), "Yes", "No "));
 * confirmWindow.show(target);
 * </PRE>
 */
public class ConfirmDialog extends Panel{

	/**
	 * コンストラクタ. デフォルトのボタンラベル
	 * @param id Wicket-ID
	 * @param model 確認メッセージModel
	 */
	public ConfirmDialog(String id, IModel<String> model){
		super(id, model);
		queue(new Label("message", model.getObject()));
		queue(new Form<Void>("form"));
		queue(new AjaxButton("yes"){
			@Override
			protected void onSubmit(AjaxRequestTarget target){
				ModalCallbacks callbacks = (ModalCallbacks)getPage();
				callbacks.modalCallback(true);
				callbacks.setModalCloseActionReason(ModalCloseActionReason.APPLY);
				ModalWindow.closeCurrent(target);
			}
		});
		queue(new AjaxButton("no"){
			@Override
			protected void onSubmit(AjaxRequestTarget target){
				ModalCallbacks callbacks = (ModalCallbacks)getPage();
				callbacks.modalCallback(false);
				callbacks.setModalCloseActionReason(ModalCloseActionReason.CANCEL);
				ModalWindow.closeCurrent(target);
			}
		});
	}
	/**
	 * コンストラクタ.
	 * @param id Wicket-ID
	 * @param model 確認メッセージModel
	 * @param applyLabel apply 左側ボタンラベル
	 * @param cancelLabel cancel 右側ボタンラベル
	 */
	public ConfirmDialog(String id, IModel<String> model, String applyLabel, String cancelLabel){
		super(id, model);
		queue(new Label("message", model.getObject()));
		queue(new Form<Void>("form"));
		queue(new AjaxButton("yes"){
			@Override
			public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag){
				replaceComponentTagBody(markupStream, openTag, applyLabel);
			}
			@Override
			protected void onSubmit(AjaxRequestTarget target){
				ModalCallbacks callbacks = (ModalCallbacks)getPage();
				callbacks.modalCallback(true);
				callbacks.setModalCloseActionReason(ModalCloseActionReason.APPLY);
				ModalWindow.closeCurrent(target);
			}
		});
		queue(new AjaxButton("no"){
			@Override
			public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag){
				replaceComponentTagBody(markupStream, openTag, cancelLabel);
			}
			@Override
			protected void onSubmit(AjaxRequestTarget target){
				ModalCallbacks callbacks = (ModalCallbacks)getPage();
				callbacks.modalCallback(false);
				callbacks.setModalCloseActionReason(ModalCloseActionReason.CANCEL);
				ModalWindow.closeCurrent(target);
			}
		});
	}
	@Override
	protected void onAfterRender() {
		super.onAfterRender();
		((ModalCallbacks)getPage()).initModalCloseActionReason();
	}
}
