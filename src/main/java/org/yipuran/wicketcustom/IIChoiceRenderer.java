package org.yipuran.wicketcustom;

import java.util.List;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

/**
 * IChoiceRendererの拡張. IChoiceRendererの getDisplayValue をラムダ式で使う目的
 * <PRE>
 * XDropDownChoice（DropDownChoiceの拡張）をIChoiceRendererの getDisplayValue をラムダ式で書く為の関数型インターフェース
 * </PRE>
 * @param <T>
 */
@FunctionalInterface
public interface IIChoiceRenderer<T extends ChoiceValue> extends IChoiceRenderer<T>{
	@Override
	public default String getIdValue(T t, int index){
		return ((ChoiceValue)t).getOptionValue();
	}
	@Override
	public default T getObject(String value, IModel<? extends List<? extends T>> models){
		if (value==null) return null;
		return models.getObject().stream().filter(e->value.equals(((ChoiceValue)e).getOptionValue())).findFirst().orElse(null);
	}
}
