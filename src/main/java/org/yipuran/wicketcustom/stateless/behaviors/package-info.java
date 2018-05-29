/**
 * Stateless AJAX behaviors.
 * <PRE>
 * (StatelessAjaxFormSubmitBehavior の 使用例)
 *
 * queue(new Button("submit").add(StatelessAjaxFormSubmitBehavior.onSubmit("click", SerialThrowableConsumer.of(t->{
 * 		feedbackError_flg = false;
 * 		String login = loginname_Field.getModelObject();
 * 		String passwd = passwd_Field.getModelObject();
 * 		if (passwd==null || login==null){
 * 			throw new RuntimeException("NULL input");
 * 		}
 * 		// OK execute!
 * 	}, (t, x)->{
 * 		feedbackError_flg = true;
 * 		feedback.error("エラー");
 * 		t.add(feedback);
 * 	}))));
 *
 * </PRE>
 */
package org.yipuran.wicketcustom.stateless.behaviors;
