package org.yipuran.wicketcustom.modal;

/**
 * ModalWindowからCallbackするメソッド定義.
 */
public interface ModalCallbacks{
	/**
	 * モーダルウィンドウ閉じた理由を初期化する.
	 */
	public void initModalCloseActionReason();

	/**
	 * @return modalCloseActionReason モーダルウィンドウ閉じた理由.
	 */
	public ModalCloseActionReason getModalCloseActionReason();

	/**
	 * @param modalCloseActionReason モーダルウィンドウ閉じた理由をセット
	 */
	public void setModalCloseActionReason(ModalCloseActionReason reason);

	/**
	 * ModalWindowから呼び出される処理.
	 * @param parameters パラメータ
	 */
	public void modalCallback(Object...parameters);

}
