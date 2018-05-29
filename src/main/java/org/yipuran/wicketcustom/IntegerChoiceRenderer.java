package org.yipuran.wicketcustom;

import java.util.List;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

/**
 * Integer型 DropDownChoice 用 IChoiceRenderer. optionタグ value 属性に Integer 値とするため。
 * <PRE>
 * （例） 整数１～１２までの DropDownChoice
 *
 * new DropDownChoice<>("select", new Model<>(), IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()), new IntegerChoiceRenderer());
 *
 * </PRE>
 */
public class IntegerChoiceRenderer implements IChoiceRenderer<Integer>{
	@Override
	public Object getDisplayValue(Integer i){
		return i;
	}
	@Override
	public String getIdValue(Integer i, int index){
		return i.toString();
	}
	@Override
	public Integer getObject(String value, IModel<? extends List<? extends Integer>> model){
		return model.getObject().stream().filter(e->Integer.toString(e).equals(value)).findFirst().orElse(null);
	}
}
