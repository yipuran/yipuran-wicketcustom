package org.yipuran.wicketcustom;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;

/**
 * JavaScript window.open 実行スクリプト生成.
 * <PRE>
 * window.open オプションの内、  menubar=no, toolbar=no, scrollbars=yes は固定で
 * 対象パス、タイトル、初期表示サイズ（width , heiht）を指定して
 * JavaScript で実行するためのスクリプトを生成
 *
 * （使用例）
 *
 *     queue(new Button("submit").add(AjaxFormSubmitBehavior.onSubmit("click", t-&gt;{
 *
 *        Windowloader wloader = Windowloader.of(this, OtherPage.class)
 *                 .addParam("a", "1").addParam("b", "2").setFrame(1000, 400);
 *        t.appendJavaScript(wloader.script());
 *
 *     })));
 *
 * </PRE>
 */
public final class Windowloader{
	private String targetPath;
	private Map<String, String> params;
	private String title = "";
	private Integer width  = 400;
	private Integer height = 400;

	/** private consturctor. */
	private Windowloader(String targetPath){
		params = new HashMap<>();
		this.targetPath = targetPath;
	}
	/**
	 * open パス指定でインスタンス生成.
	 * @param path window.open の第１引数を指定
	 * @return Windowloader
	 */
	public static Windowloader of(String path){
		return new Windowloader(path);
	}
	/**
	 * 表示元 WebPage + 表示対象 Page指定でインスタンス生成.
	 * @param page 表示元 WebPage、通常 WebPage内の記述で this を指定
	 * @param cls 表示対象 Pageのクラス
	 * @return Windowloader
	 */
	public static Windowloader of(WebPage page, Class<? extends Page> cls){
		return new Windowloader(page.getRequestCycle().getRequest().getContextPath() + "/wicket/bookmarkable/" + cls.getName());
	}
	/**
	 * ＵＲＬ追加パラメータの設定.
	 * @param key キー
	 * @param value 値
	 * @return Windowloader
	 */
	public Windowloader addParam(String key, String value){
		if (key != null && key.length() > 0 && value != null){
			params.put(key, value);
		}
		return this;
	}
	/**
	 * タイトル設定.
	 * @param title ウィンドウタイトル
	 * @return Windowloader
	 */
	public Windowloader setTitle(String title){
		if (title != null){
			this.title = title;
		}
		return this;
	}
	/**
	 * ウィンドウウィンドウ表示サイズ設定.
	 * このメソッドを実行しないと、デフォルト width=400 , height=400 になる。
	 * @param width 幅
	 * @param height 高さ
	 * @return
	 */
	public Windowloader setFrame(int width, int height){
		if (width > 0 && height > 0){
			this.width = width;
			this.height = height;
		}
		return this;
	}

	/**
	 * window.open 実行スクリプト生成
	 * @return JavaScipt としてそのまま実行できるスクリプト
	 */
	public String script(){
		StringBuilder sb = new StringBuilder();
		sb.append("window.open('");
		sb.append(path());
		// title
		sb.append("','");
		sb.append(title);
		// 幅
		sb.append("','width=");
		sb.append(width);
		// 高さ
		sb.append(",height=");
		sb.append(height);

		sb.append(", menubar=no, toolbar=no, scrollbars=yes');");
		return sb.toString();
	}
	/**
	 * window.open 第１引数に渡す PATH 生成
	 * @return window.open 第１引数に渡す PATH
	 */
	public String path(){
		if (params.size() > 0){
			return targetPath + "?" +
			params.entrySet().stream().map(e->e.getKey() + "=" + e.getValue())
			.collect(Collectors.joining("&"));
		}
		return targetPath;
	}
}
