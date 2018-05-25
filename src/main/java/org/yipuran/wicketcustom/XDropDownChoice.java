package org.yipuran.wicketcustom;

import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

/**
 * DropDownChoiceの拡張.
 * <PRE>
 * final XDropDownChoice<Item> select_item = new DropDownChoiceEx<Item>("select_item", new Model<Item>(), createItemList(), e->e.name );
 * </PRE>
 * @param <T>
 */
public class XDropDownChoice<T> extends DropDownChoice<T>{
	/**
	 * コンストラクタ.
	 * @param id Wicket-ID
	 * @param model 初期表示選択肢
	 * @param choices 選択リスト
	 * @param renderer IChoiceRenderer拡張
	 * @param <T>
	 */
	public XDropDownChoice(String id, IModel<T> model, List<? extends T> choices, IIChoiceRenderer<? super T> renderer){
		super(id, model, choices, renderer);
	}
	/**
	 * コンストラクタ.
	 * @param id Wicket-ID
	 * @param model 初期表示選択肢Model
	 * @param choices 選択リスト
	 * @param renderer IChoiceRenderer拡張
	 * @param <T>
	 */
	public XDropDownChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices, IIChoiceRenderer<? super T> renderer){
		super(id, model, choices, renderer);
	}
	/**
	 * コンストラクタ.
	 * @param id Wicket-ID
	 * @param choices 選択リスト
	 * @param renderer IChoiceRenderer拡張
	 * @param <T>
	 */
	public XDropDownChoice(String id, List<? extends T> choices, IIChoiceRenderer<? super T> renderer){
		super(id, choices, renderer);
	}
	/**
	 * コンストラクタ.
	 * @param id Wicket-ID
	 * @param choices 選択リスト
	 * @param renderer IChoiceRenderer拡張
	 * @param <T>
	 */
	public XDropDownChoice(String id, IModel<? extends List<? extends T>> choices, IIChoiceRenderer<? super T> renderer){
		super(id, choices, renderer);
	}
}
