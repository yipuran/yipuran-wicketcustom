package org.yipuran.wicketcustom;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;

/**
 * No PageParameters Encoder.
 * <PRE>
 * Usage:
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
