package org.yipuran.wicketcustom.ajax;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * YAjaxTabbedPanel = AjaxTabbedPanel の継承.
 * <PRE>
 * Tab クリック時にクリックする前の Tabインデックスを捕捉し onBeforeChangeTab をタブ切り替え前に実行する。
 * onBeforeChangeTab メソッドオーバーライドして使用する。
 * </PRE>
 * @param <T> T
 */
public class YAjaxTabbedPanel<T extends ITab> extends AjaxTabbedPanel<T>{

	/**
	 * コンストラクタ.
	 * @param id Wicket-ID
	 * @param tabs タブComponentリスト
	 */
	public YAjaxTabbedPanel(String id, List<T> tabs){
		super(id, tabs);
	}
	/**
	 * コンストラクタ.
	 * @param id Wicket-ID
	 * @param tabs タブComponentリスト
	 * @param model IModel
	 */
	public YAjaxTabbedPanel(String id, List<T> tabs, IModel<Integer> model){
		super(id, tabs, model);
	}
	@Override
	protected WebMarkupContainer newLink(final String linkId, final int index){
		return new AjaxSubmitLink(linkId){
			@Override
			protected void onSubmit(AjaxRequestTarget target){
				onBeforeChangeTab(target, index);
				setSelectedTab(index);
				if (target != null){
					target.add(YAjaxTabbedPanel.this);
				}
				onAjaxUpdate(Optional.ofNullable(target));
			}
		};
	}
	/**
	 * Tabクリック前捕捉処理.
	 * @param target AjaxRequestTarget
	 * @param index クリックした Tab インデックス
	 */
	protected void onBeforeChangeTab(final AjaxRequestTarget target, final int index){
	}
}
