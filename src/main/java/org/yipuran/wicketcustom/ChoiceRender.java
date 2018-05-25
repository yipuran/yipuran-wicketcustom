package org.yipuran.wicketcustom;

import java.util.List;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.danekja.java.util.function.serializable.SerializableBiPredicate;
import org.danekja.java.util.function.serializable.SerializableFunction;

/**
 * ChoiceRender.
 * <PRE>
 * final DropDownChoice<Foo> selectFoo = new DropDownChoice<Foo>("select1", new Model<>(), Arrays.asList(new Foo(1,"A"), new Foo(2,"B"), new Foo(3,"C") )
 *    , ChoiceRender.of(t->Integer.toString(t.val), t->t.name, (t,u)->Integer.toString(t.val).equals(u)) ) ;
 * queue(selectFoo);
 *
 * final DropDownChoice<String> selectChar = new DropDownChoice<String>("select2", new Model<>(), Arrays.asList("a","b","c")
 *    , ChoiceRender.of(t->t, t->t, (t,u)->t.equals(u)) ) ;
 * queue(selectChar);
 * </PRE>
 */
public final class ChoiceRender{
	private ChoiceRender(){}

	public static <T> IChoiceRenderer<T> of(SerializableFunction<T, String> idfunction
			, SerializableFunction<T, String> displayfunction
			, SerializableBiPredicate<T, String> bipredicate
	){
		return new IChoiceRenderer<T>(){
			@Override
			public Object getDisplayValue(T t){
				return displayfunction.apply(t);
			}
			@Override
			public String getIdValue(T t, int index){
				return idfunction.apply(t);
			}
			@Override
			public T getObject(String id, IModel<? extends List<? extends T>> choices){
				return choices.getObject().stream().filter(e->bipredicate.test(e, id)).findFirst().orElse(null);
			}
		};
	}
}
