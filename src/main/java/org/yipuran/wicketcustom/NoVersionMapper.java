package org.yipuran.wicketcustom;

import org.apache.wicket.core.request.handler.BookmarkableListenerRequestHandler;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;

/**
 * URLからページバージョン番号を除外する mount Mapper.
 * <PRE>
 * 使用例 WebApplocation の init() の中で各々の WebPage に対して指定する
 *     getRootRequestMapperAsCompound().add(new NoVersionMapper("/foo", FooPage.class));
 * </PRE>
 */
public class NoVersionMapper extends MountedMapper{
	/**
	 * コンストラクタ.
	 * @param mountPath URL-pathを contextの次("/context") から "/" で初めて指定
	 * @param pageClass 対象Page Class
	 */
	public NoVersionMapper(String mountPath, final Class<? extends IRequestablePage> pageClass){
		super(mountPath, pageClass, new PageParametersEncoder());
	}
	/* @see org.apache.wicket.core.request.mapper.AbstractComponentMapper#encodePageComponentInfo(org.apache.wicket.request.Url, org.apache.wicket.request.mapper.info.PageComponentInfo) */
	@Override
	protected void encodePageComponentInfo(Url url, PageComponentInfo info){
	}
	/* @see org.apache.wicket.core.request.mapper.MountedMapper#mapHandler(org.apache.wicket.request.IRequestHandler) */
	@Override
	public Url mapHandler(IRequestHandler requestHandler){
		if (requestHandler instanceof ListenerRequestHandler
				|| requestHandler instanceof BookmarkableListenerRequestHandler){
			return null;
		}else{
			return super.mapHandler(requestHandler);
		}
	}
}
