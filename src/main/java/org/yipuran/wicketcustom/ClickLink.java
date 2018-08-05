package org.yipuran.wicketcustom;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.lang.Args;
import org.danekja.java.util.function.serializable.SerializableConsumer;

/**
 * ClickLink : リンク click → シリアライズ化した Consumer の実行.
 * <PRE>
 * （使用例）
 *   wicket:id="back" を記述した &lt;a&gt; タグに対して、
 *
 *      queue(ClickLink.bind("back", i-&gt;setResponsePage(HomePage.class)));
 *
 * </PRE>
 */
public abstract class ClickLink<T> extends Link<T>{

	/**
	 * private コンストラクタ.
	 * @param コンポーネントＩＤ
	 */
	private ClickLink(String id){
		super(id);
	}
	/**
	 * コンポーネントＩＤ→シリアライズ Consumer 設定コンポーネント生成.
	 * <PRE>
	 * クリックイベントを追加したいコンポーネントの ID を指定して
	 * Consumer を記述する。
	 * Consumer の引数はンポーネントのID
	 * </PRE>
	 * @param id component-ID
	 * @param onClick SerializableConsumer<String> parameter is component-ID
	 * @return ClickLink
	 */
	public static <T> ClickLink<T> bind(String id, SerializableConsumer<String> onClick){
		Args.notNull(onClick, "onClick");
		return new ClickLink<T>(id){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(){
				onClick.accept(id);
			}
		};
	}
}