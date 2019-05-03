package org.yipuran.wicketcustom.function;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Serializable And Throwable BiConsumer.
 * 例外捕捉するシリアライズな BiConsumerを必要とするケースで使用する。
 * <PRE>
 * （使用例）
 * queue(new Image("fig1", resourcerefer).add(AjaxFileDropUpdateBehavior.of((t, list)->{
 *    FileUpload fu = flist.get(0);
 *    // t=AjaxRequestTarget list=List&lt;FileUpload&gt;
 *
 * }, (t, x)->{
 *    // t=AjaxRequestTarget x=FileUploadException
 *
 * })));
 *
 *    Predicate&lt;FileUpload&gt; で制限をする例
 * queue(new Image("fig1", resourcerefer).add(AjaxFileDropUpdateBehavior.of(
 * fu->Pattern.compile("^image/(jpeg|png|gif)$").matcher(fu.getContentType().toLowerCase()).matches()
 * , (t, list)->{
 *    FileUpload fu = flist.get(0);
 *    // t=AjaxRequestTarget list=List&lt;FileUpload&gt;
 *
 * }, (t, x)->{
 *    // t=AjaxRequestTarget x=FileUploadException
 *
 * })));
 * </PRE>
 * @since 4.6
 */
public interface SerialThrowableBiConsumer<T, U> extends Serializable{
	void accept(T t, U u) throws Exception;

	default BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after, BiConsumer<SimpleEntry<T, U>, Exception> onCatch){
		Objects.requireNonNull(after);
		return (t, u) -> {
			try{
				accept(t, u);
			}catch(Exception e){
				onCatch.accept(new SimpleEntry<>(t, u), e);
			}
			after.accept(t, u);
		};
   }
	/**
	 * SerialThrowableBiConsumer 生成.
	 * @param consumer 例外スローする BiConsumer&lt;T, U&gt;処理
	 * @param onCatch Exception捕捉処理、BiConsumer&lt;SimpleEntry&lt;T, U&gt;, Exception&gt;、
	 * @return BiConsumer&lt;T, U&gt;
	 */
	public static <T, U> BiConsumer<T, U> of(SerialThrowableBiConsumer<T, U> consumer, BiConsumer<SimpleEntry<T, U>, Exception> onCatch){
		return (t, u)->{
			try{
				consumer.accept(t, u);
			}catch(Exception ex){
				onCatch.accept(new SimpleEntry<T, U>(t, u), ex);
			}
		};
	}
	/**
	 * SerialThrowableBiConsumer 生成（外に例外スロー）.
	 * @param consumer 例外スローする BiConsumer&lt;T, U&gt;処理
	 * @return BiConsumer&lt;T, U&gt;
	 */
	public static <T, U> BiConsumer<T, U> of(SerialThrowableBiConsumer<T, U> consumer){
		return (t, u)->{
			try{
				consumer.accept(t, u);
			}catch(Throwable ex){
				throw new RuntimeException(ex);
			}
		};
	}
}
