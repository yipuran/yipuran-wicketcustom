/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.yipuran.wicketcustom.stateless.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.lang.Args;
import org.danekja.java.util.function.serializable.SerializableConsumer;

/**
 * Stateless version of OnChangeAjaxBehavior.
 *
 * <p>
 * This behavior uses best available method to track changes on different types of form components.
 * To accomplish this for text input form components it uses a custom event, named 'inputchange',
 * that is handled in the best way for the specific browser. For other form component types the
 * 'change' event is used.
 * </p>
 *
 * @author Andrea Del Bene
 * Copyright © 2016 wicket-stuff
 */
public class StatelessOnChangeAjaxBehavior extends StatelessAjaxFormComponentUpdatingBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * 'inputchange' event delegates to 'input', 'keyup', 'cut' and 'paste' events
	 * for text input form component depending on the browser.
	 * 'change' is used as a fallback for all other form component types.
	 */
	public static final String EVENT_NAME = "inputchange change";

	public static final String EVENT_CHANGE = "change";

	public StatelessOnChangeAjaxBehavior()
	{
		super(EVENT_NAME);
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);

		Component component = getComponent();

		// textfiels and textareas will trigger this behavior with either 'inputchange' or 'change' events
		// all the other components will use just 'change'
		if (!(component instanceof TextField || component instanceof TextArea))
		{
			attributes.setEventNames(EVENT_CHANGE);
		};
	}

	@Override
	protected void onUpdate(AjaxRequestTarget target)
	{
	}

	/**
	 * Creates an {@link StatelessOnChangeAjaxBehavior} based on lambda expressions
	 * @param onChange the {@code StatelessOnChangeAjaxBehavior} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link StatelessOnChangeAjaxBehavior}
	 */
	public static StatelessOnChangeAjaxBehavior onChange(SerializableConsumer<AjaxRequestTarget> onChange){
		Args.notNull(onChange, "onChange");
		return new StatelessOnChangeAjaxBehavior(){
			private static final long serialVersionUID = 1L;
			@Override
			protected void onUpdate(AjaxRequestTarget target){
				onChange.accept(target);
			}
		};
	}
}
