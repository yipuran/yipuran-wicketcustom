package org.yipuran.wicketcustom.auth;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 認証 Strategy.
 * <PRE>
 * WebApplicarion の init() で、getSecuritySettings().setAuthorizationStrategy でインスタンスをセットする。
 * セッション有効期限切れもチェックする。
 * セッションは、AuthSession を継承して有効にすること。
 * （例）
 * 　　getSecuritySettings().setAuthorizationStrategy(new AuthorizationStrategy(BasePage.class, LoginPage.class));
 * </PRE>
 */
public class AuthorizationStrategy implements IAuthorizationStrategy{
	private Class<? extends WebPage> authenticatedPage;
	private Class<? extends WebPage> signinPage;

	Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * コンストラクタ.
	 * @param authenticatedPage 認証済の基底 WebPage class
	 * @param signinPage 認証を行う WebPage class
	 */
	public AuthorizationStrategy(Class<? extends WebPage> authenticatedPage, Class<? extends WebPage> signinPage){
		this.authenticatedPage = authenticatedPage;
		this.signinPage = signinPage;
	}
	/* @see org.apache.wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class) */
	@Override
	public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> component){
		return true;
	}
	/**
	 * 認証チェック.
	 * セッションに認証済か問い合わせて認証済なら trueを返す。認証できないまたは有効期限切れは
	 * RestartResponseAtInterceptPageException でコンストラクタで指定した signinPage に遷移する
	 * @param component Component
	 * @param action Action
	 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component, org.apache.wicket.authorization.Action)
	 */
	@Override
	public boolean isActionAuthorized(Component component, Action action){
		if (authenticatedPage.isAssignableFrom(component.getClass())){
			// セッションに認証済か問い合わせて認証済なら true
			if (((AuthSession)Session.get()).isAuth()){
				if (((AuthSession)Session.get()).overtime()){
					// セッション有効切れ
					throw new RestartResponseAtInterceptPageException(signinPage);
				}
				return true;
			}
			throw new RestartResponseAtInterceptPageException(signinPage);
		}
		return true;
	}

	/* @see org.apache.wicket.authorization.IAuthorizationStrategy#isResourceAuthorized(org.apache.wicket.request.resource.IResource, org.apache.wicket.request.mapper.parameter.PageParameters) */
	@Override
	public boolean isResourceAuthorized(IResource resource, PageParameters parameters){
		if (((AuthSession)Session.get()).isAuth()){
			if (((AuthSession)Session.get()).overtime()){
				return false;
			}
			return true;
		}
		return true;
	}
}
