/**
 * 認証機能パッケージ.
 * <PRE>
 * wicket-auth の Role を使用するわけではなくセッションで認証済をチェックしてPage のアクセスを制限するもの。
 * org.apache.wicket.authorization.IAuthorizationStrategy を実践するパッケージ
 *
 *    AuthorizationStrategy と AuthSession を提供している。
 *
 * WebApplicarion の init() で、getSecuritySettings().setAuthorizationStrategy の実行を
 * AuthorizationStrategy インスタンス生成して実行する。
 *
 *      // BasePage.class  : 認証チェック対象Page の基底クラス
 *      // LoginPage.class : 認証サインするPage のクラス
 *      getSecuritySettings().setAuthorizationStrategy(new AuthorizationStrategy(BasePage.class, LoginPage.class));
 *
 * 有効にする WebSession として、AuthSessionを継承する。
 * コンストラクタで認証済からアクセスできる有効期間を秒単位で指定する。
 * 用意するセッションクラスで認証済をセットする時に、AuthSession の setAuth() を実行する。
 * setAuth() 実行の都度、有効期間の経過時間は初期化される。
 *     public class MySession extends AuthSession{
 *         public SalonSession(Request request){
 *            // セッション有効期限  1800秒
 *            super(request, 1800);
 *         }
 *         // 認証セットで AuthSession の setAutn を実行
 *         // このメソッドは認証処理の中で、getSession() で取得したセッションで実行
 *         public void setAuthId(String authid){
 *            setAuth();
 *            // 認証セット処理
 *         }
 *     }
 * </PRE>
 */
package org.yipuran.wicketcustom.auth;