package org.yipuran.wicketcustom.ajax;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.util.resource.IResourceStream;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableSupplier;

/**
 * AJAX Download.
 * <PRE>
 * final AJAXDownload download = new AJAXDownload(){
 * 	@Override
 * 	protected IResourceStream getResourceStream(){
 * 		return new AbstractResourceStreamWriter(){
 * 			@Override
 * 			public void write(OutputStream out) throws IOException{
 *
 * 			}
 * 			@Override
 * 			public String getContentType(){
 * 				//return "text/comma-separated-values";
 * 				return "text/csv";
 * 			}
 *			};
 *		}
 *		@Override
 *		protected String getFileName(){
 *			return "aaa.csv";
 *		}
 *	};
 *	queue(new AjaxButton("csvout"){
 *		@Override
 *		protected void onSubmit(AjaxRequestTarget target, Form<?> form){
 *			// AJAXダウンロード コールバック実行
 *			download.callBackDownload(target);
 *		}
 *	}).add(download);
 * </PRE>
 */
public abstract class AJAXDownload extends AbstractAjaxBehavior{
	/**
	 * ダウンロードIResourceStream取得.
	 * @return IResourceStream
	 */
	protected abstract IResourceStream getResourceStream();
	/**
	 * ダウンロード実行.
	 * @param target AjaxRequestTarget
	 */
	public void callBackDownload(AjaxRequestTarget target){
		target.appendJavaScript("setTimeout(\"window.location.href='" + getCallbackUrl().toString() + "'\", 100);");
	}
	/* @see org.apache.wicket.behavior.IBehaviorListener#onRequest() */
	@Override
	public void onRequest(){
		try{
			//ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(getResourceStream(), URLEncoder.encode(getFileName(), "UTF-8"));
			ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(getResourceStream(), getFileName());
			handler.setContentDisposition(ContentDisposition.ATTACHMENT);
			getComponent().getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
		}catch(Exception e){
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	/**
	 * ダウンロードファイル名決定.
	 * @return getFileName ダウンロードファイル名
	 */
	protected String getFileName(){
		return null;
	}

	/**
	 * AJAXDownloadインスタンス生成.
	 * (Usage)
	 *  final AJAXDownload download = AJAXDownload.of(out->{
	 *     // out への出力
	 *  }, ()->"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ()->"foo.xlsx");
	 *  compoment.add(download);
	 *  queue(AjaxLink.onClick("download", t->{
	 *     // 出力準備の後で、、
	 *     download.callBackDownload(t);
	 *  }).add(download));
	 *
	 * @param write OutputStream の Consume<OutputStream>
	 * @param getConteType content-type の Supplier<String>
	 * @param getName ダウンロードファイル名の Supplier<String>
	 * @return AJAXDownload
	 */
	public static AJAXDownload of(SerializableConsumer<OutputStream> write, SerializableSupplier<String> getConteType, SerializableSupplier<String> getName){
		return new AJAXDownload(){
			@Override
			protected IResourceStream getResourceStream(){
				return new AbstractResourceStreamWriter(){
					@Override
					public void write(OutputStream out) throws IOException{
						write.accept(out);
						out.close();
					}
					@Override
					public String getContentType(){
						return getConteType.get();
					}
				};
			}
			@Override
			protected String getFileName(){
				return getName.get();
			}
		};
	}
}
