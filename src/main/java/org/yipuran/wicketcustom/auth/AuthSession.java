package org.yipuran.wicketcustom.auth;

import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

/**
 * 認証チェック機能 WebSession.
 * <PRE>
 * AuthorizationStrategy とともに使用する。
 * 例）
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
public class AuthSession extends WebSession{
	private boolean auth = false;
	private LocalDateTime actionTime;
	private int overtimeLimit = 1800;
	/**
	 * コンストラクタ.
	 * @param request org.apache.wicket.request
	 * @param overtimeLimit セッション有効期限 秒単位指定
	 */
	public AuthSession(Request request, int overtimeLimit){
		super(request);
		this.overtimeLimit = overtimeLimit;
		actionTime = LocalDateTime.now();
	}
	/**
	 * 認証済問い合わせ.
	 * @return true=認証済
	 */
	public boolean isAuth(){
		return auth;
	}
	/**
	 * 認証済を初期化（認証否にする）.
	 */
	public void initAuth(){
		auth = false;
	}
	/**
	 * 認証済セット. setAuth() 実行の都度、有効期間の経過時間は初期化される。
	 * @param isAuth ログイン成功したら true をセットする
	 */
	public void setAuth(){
		auth = true;
		actionTime = LocalDateTime.now();
	}
	/**
	 * 一定時間経過後の実行問い合わせ.
	 * @return true=時間経過している。
	 */
	public boolean overtime(){
		if (Duration.between(actionTime, LocalDateTime.now()).getSeconds() > overtimeLimit){
			return true;
		}
		actionTime = LocalDateTime.now();
		return false;
	}
}
