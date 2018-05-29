package org.yipuran.wicketcustom.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;

/**
 * GenericAjaxEventBehavior.
 * <PRE>
 * AJAX イベントビヘビアを Consumer ラムダで記述する。
 *    link.add(GenericAjaxEventBehavior.of("click", (c, t)->{
 *        // t = AjaxRequestTarget 処理する
 *    });
 * </PRE>
 */
public abstract class GenericAjaxEventBehavior extends AjaxEventBehavior{

	/**
	 * private コンストラクタ.
	 */
	private GenericAjaxEventBehavior(String event){
		super(event);
	}

	public static GenericAjaxEventBehavior of(String eventName, SerializableBiConsumer<Component, AjaxRequestTarget> consumer){
		return new GenericAjaxEventBehavior(eventName){
			private static final long serialVersionUID = 1L;
			@Override
			protected void onEvent(AjaxRequestTarget target){
				consumer.accept(getComponent(), target);
			}
		};
	}
}
