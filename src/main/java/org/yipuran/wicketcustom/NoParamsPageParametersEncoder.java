package org.yipuran.wicketcustom;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;

/**
 * No PageParameters Encoder.
 * （注意）
 *   getRequestCycle().getRequest().getQueryParameters() による IRequestPatameters からの取得は不可能になる。
 *
 * <PRE>
 * （注意）
 *   getRequestCycle().getRequest().getQueryParameters() による IRequestPatameters からの取得は不可能になる。
 *   従って PageParameter による送信は呼出し側からページインスタンス生成で渡す必要があり、
 *         PageParameters parameters = new PageParameters();
 *         setResponsePage(new FooPage(parameters));
 *   の手段が必要である。
 *
 *   NoVersionMapper を使用した場合はこの NoParamsPageParametersEncoder で MountedMapper、mountをしても無効で
 *   NoVersionMapper が有効になる。
 * （例）
 *
 * at WebApplication  init() method
 *
 *    mount(new MountedMapper("/foo", FooPage.class, new NoParamsPageParametersEncoder()));
 *
 * FooPage is
 *    public class FooPage extends WebPage implements IRequestablePage{
 *        public TestPage(PageParameters parameters){
 *           int a = Optional.ofNullable(parameters.get("a").toOptionalInteger()).map(e->e.intValue()).orElse(0);
 *           String b = parameters.get("b").toOptionalString();
 *
 * Call sample is
 *     queue(new Button("foo Page").add(AjaxEventBehavior.onEvent("click", t->{
 *        PageParameters parameters = new PageParameters();
 *        parameters.add("a", 10);
 *        parameters.add("b", "Hello");
 *        setResponsePage(new FooPage(parameters));
 *     })));
 *
 * </PRE>
 */
public class NoParamsPageParametersEncoder extends PageParametersEncoder{
	/**
	 * コンストラクタ.
	 */
	public NoParamsPageParametersEncoder(){
      super();
   }
   @Override
   public Url encodePageParameters(PageParameters pageParameters) {
      return new Url();
   }
}
