package org.yipuran.wicketcustom.ajax;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.AjaxFileDropBehavior;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.yipuran.wicketcustom.function.SerialThrowableBiConsumer;

/**
 * ファイルドロップビヘビア・関数型ラッパー.
 * <PRE>
 * dragover イベントでファイルアップ―ロードを実行するビヘビア org.apache.wicket.extensions.ajax.AjaxFileDropBehavior
 * を継承してラムダ式で、アップロードした List&lt;FileUpload&gt; 処理、エラー処理を記述できるようにする。
 * Serializable And Throwable BiConsumer である org.yipuran.wicketcustom.function.SerialThrowableBiConsumer で
 * SerialThrowableBiConsumer&lt;AjaxRequestTarget, List&lt;FileUpload&gt;&gt; と BiConsumer&lt;AjaxRequestTarget, FileUploadException&gt;
 * SerialThrowableBiConsumer&lt;AjaxRequestTarget, List&lt;FileUpload&gt;&gt 内で発生する例外は、
 * BiConsumer&lt;AjaxRequestTarget, FileUploadException&gt; の FileUploadException でラップされて処理される。
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
 * アップロードするファイルをPredicate&lt;FileUpload&gt; で制限をする例
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
public class AjaxFileDropUpdateBehavior extends AjaxFileDropBehavior{
	private SerialThrowableBiConsumer<AjaxRequestTarget, List<FileUpload>> c;
	private BiConsumer<AjaxRequestTarget, FileUploadException> err;
	private Predicate<FileUpload> p;

	private AjaxFileDropUpdateBehavior(SerialThrowableBiConsumer<AjaxRequestTarget, List<FileUpload>> upload, BiConsumer<AjaxRequestTarget, FileUploadException> error){
		c = upload;
		err = error;
	}
	private AjaxFileDropUpdateBehavior(Predicate<FileUpload> pre, SerialThrowableBiConsumer<AjaxRequestTarget, List<FileUpload>> up, BiConsumer<AjaxRequestTarget, FileUploadException> error){
		c = up;
		err = error;
		p = pre;
	}
	/**
	 * AjaxFileDropUpdateBehavior生成.
	 * @param up AjaxRequestTarget と List&lt;FileUpload&gt; の Consumer
	 * @param error AjaxRequestTarget と FileUploadException の Consumer
	 * @return AjaxFileDropUpdateBehaviorインスタンス
	 */
	public static AjaxFileDropUpdateBehavior of(SerialThrowableBiConsumer<AjaxRequestTarget, List<FileUpload>> up, BiConsumer<AjaxRequestTarget, FileUploadException> error){
		return new AjaxFileDropUpdateBehavior(up, error);
	}
	/**
	 * 制限付き AjaxFileDropUpdateBehavior生成.
	 * @param pre FileUpload の Predicate
	 * @param up AjaxRequestTarget と List&lt;FileUpload&gt; の Consumer
	 * @param error AjaxRequestTarget と FileUploadException の Consumer
	 * @return AjaxFileDropUpdateBehaviorインスタンス
	 */
	public static AjaxFileDropUpdateBehavior of(Predicate<FileUpload> pre, SerialThrowableBiConsumer<AjaxRequestTarget, List<FileUpload>> up, BiConsumer<AjaxRequestTarget, FileUploadException> error){
		return new AjaxFileDropUpdateBehavior(pre, up, error);
	}

	@Override
	protected void onError(AjaxRequestTarget target, FileUploadException fux){
		err.accept(target, fux);
	}
	@Override
	protected void onFileUpload(AjaxRequestTarget target, List<FileUpload> files){
		try{
			if (p==null){
				c.accept(target, files);
			}else{
				if (files.stream().anyMatch(p)){
					c.accept(target, files.stream().filter(p).collect(Collectors.toList()));
				}
			}
		}catch(Exception e){
			err.accept(target, new FileUploadException(e.getMessage(), e));
		}
	}
}
