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
 *  Copyright © 2016 wicket-stuff
 */
package org.yipuran.wicketcustom.stateless.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.yipuran.wicketcustom.stateless.StatelessEncoder;

/**
 * Stateless version of AjaxEventBehavior
 *
 * @author Andrea Del Bene
 *
 */
public abstract class StatelessAjaxEventBehavior extends AjaxEventBehavior
{
    private static final long serialVersionUID = 2387070289758596955L;

    public StatelessAjaxEventBehavior(final String event)
    {
        super(event);
    }

    @Override
    protected void onBind()
    {
        super.onBind();

        //generate behavior id
        getComponent().getBehaviorId(this);
    }

    @Override
    public CharSequence getCallbackUrl()
    {
        final Url url = Url.parse(super.getCallbackUrl().toString());
        final PageParameters params = getPageParameters();

        return StatelessEncoder.mergeParameters(url, params).toString();
    }

    protected abstract PageParameters getPageParameters();

    /**
     * @return always {@literal true}
     */
    @Override
    public boolean getStatelessHint(final Component component)
    {
        return true;
    }
    /**
	 * Creates an {@link StatelessAjaxEventBehavior} based on lambda expressions
	 * @param eventName the event name
	 * @param onEvent the {@code SerializableConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link StatelessAjaxEventBehavior}
	 */
	public static StatelessAjaxEventBehavior onEvent(String eventName, SerializableConsumer<AjaxRequestTarget> onEvent){
		Args.notNull(onEvent, "onEvent");
		return new StatelessAjaxEventBehavior(eventName)	{
			private static final long serialVersionUID = 1L;
			@Override
			protected void onEvent(AjaxRequestTarget target){
				onEvent.accept(target);
			}
			@Override
			protected PageParameters getPageParameters(){
				return null;
			}
		};
	}
}
