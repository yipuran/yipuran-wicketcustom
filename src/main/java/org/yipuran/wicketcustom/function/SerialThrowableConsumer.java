package org.yipuran.wicketcustom.function;

import java.util.Objects;
import java.util.function.Consumer;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableConsumer;

/**
 * Serializable And Throwable Consumer.
 * <PRE>
 * AjaxFormSubmitBehavior.onSubmit 等、例外捕捉するシリアライズな Consumerを必要とするケースで使用する。
 * 提供する of メソッドで生成する。
 * （使用例）
 * queue(new Button("send").add(AjaxFormSubmitBehavior.onSubmit("click", SerialAndThrowableConsumer.of(t->{
 *   // t = AjaxRequestTarget
 *   label.setDefaultModelObject(infield.getValue());
 *   t.add(response);
 * }, (t, ex)->{
 *   	t.add(response);
 * }))));
 *
 *    例外捕捉処理を書かない of(SerialThrowableConsumer<T> consumer) メソッドは、Wicketの Page としては
 *    書くべきでない。RequestCycle で例外捕捉しなくてはならなくなるからだ。
 * AjaxFormSubmitBehavior.onSubmit 以外の他の使い道の候補
 *    OnChangeAjaxBehavior.onChange
 *    AjaxFormChoiceComponentUpdatingBehavior.onUpdateChoice
 *    AjaxFormComponentUpdatingBehavior.onUpdate
 *    AjaxEventBehavior.onEvent
 * </PRE>
 */
public interface SerialThrowableConsumer<T> extends SerializableConsumer<T>{
	default SerializableConsumer<T> andThen(Consumer<? super T> after, SerializableBiConsumer<T, Exception> onCatch){
		Objects.requireNonNull(after);
		return (T t)->{
			try{
				accept(t);
			}catch(Exception e){
				onCatch.accept(t, e);
			}
			after.accept(t);
		};
	}
	/**
	 * SerializableConsumer 生成.
	 * @param consumer 例外スローするシリアライズな Consumer<T>処理
	 * @param onCatch Exception捕捉処理
	 * @return SerializableConsumer<T>
	 */
	public static <T> SerializableConsumer<T> of(SerialThrowableConsumer<T> consumer, SerializableBiConsumer<T, Exception> onCatch){
		return t->{
			try{
				consumer.accept(t);
			}catch(Exception ex){
				onCatch.accept(t, ex);
			}
		};
	}
	/**
	 * SerializableConsumer 生成（外に例外スロー）.
	 * @param consumer 例外スローするシリアライズな Consumer<T>処理
	 * @return
	 */
	public static <T> SerializableConsumer<T> of(SerialThrowableConsumer<T> consumer){
		return t->{
			try{
				consumer.accept(t);
			}catch(Throwable ex){
				throw new RuntimeException(ex);
			}
		};
	}
}
