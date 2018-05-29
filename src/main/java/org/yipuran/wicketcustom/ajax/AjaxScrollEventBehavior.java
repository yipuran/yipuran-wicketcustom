package org.yipuran.wicketcustom.ajax;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.danekja.java.util.function.serializable.SerializableConsumer;

/**
 * AjaxScrollEventBehavior.
 *  <PRE>
 *    （使用例）スクロールイベントを持つコンポーネントで付与する
 *
 *    component.add(AjaxScrollEventBehavior.onScroll(t->{
 *       // todo
 *    });
 * </PRE>
 */
public abstract class AjaxScrollEventBehavior extends AjaxEventBehavior{

	/**
	 * スクロール時の処理.
	 * @param target AjaxRequestTarget
	 */
	protected abstract void onScroll(AjaxRequestTarget target);

	/**
	 * コンストラクタ.
	 */
	public AjaxScrollEventBehavior(){
		super("scroll");
	}
	/* @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget) */
	@Override
	protected void onEvent(AjaxRequestTarget target){
		onScroll(target);
	}
	private AjaxScrollEventBehavior(String event){
		super(event);
	}
	/**
	 * onScrollビヘビア登録.
	 * <PRE>
	 *    component.add(AjaxScrollEventBehavior.onScroll(t->{
	 *       // todo
	 *    });
	 * </PRE>
	 * @param scrollConsumer AjaxRequestTargetのConsumer
	 * @return AjaxScrollEventBehavior
	 */
	public static AjaxScrollEventBehavior onScroll(SerializableConsumer<AjaxRequestTarget> scrollConsumer){
		return new AjaxScrollEventBehavior("scroll"){
			private static final long serialVersionUID = 1L;
			@Override
			protected void onScroll(AjaxRequestTarget target){
				scrollConsumer.accept(target);
			}
		};
	}
}

