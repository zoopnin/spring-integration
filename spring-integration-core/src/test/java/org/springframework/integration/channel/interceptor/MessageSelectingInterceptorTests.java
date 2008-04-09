/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.channel.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import org.springframework.integration.channel.SimpleChannel;
import org.springframework.integration.message.Message;
import org.springframework.integration.message.MessageDeliveryException;
import org.springframework.integration.message.StringMessage;
import org.springframework.integration.message.selector.MessageSelector;

/**
 * @author Mark Fisher
 */
public class MessageSelectingInterceptorTests {

	@Test
	public void testSingleSelectorAccepts() {
		final AtomicInteger counter = new AtomicInteger();
		MessageSelector selector = new TestMessageSelector(true, counter);
		MessageSelectingInterceptor interceptor = new MessageSelectingInterceptor(selector);
		SimpleChannel channel = new SimpleChannel();
		channel.addInterceptor(interceptor);
		assertTrue(channel.send(new StringMessage("test1")));
	}

	@Test(expected=MessageDeliveryException.class)
	public void testSingleSelectorRejects() {
		final AtomicInteger counter = new AtomicInteger();
		MessageSelector selector = new TestMessageSelector(false, counter);
		MessageSelectingInterceptor interceptor = new MessageSelectingInterceptor(selector);
		SimpleChannel channel = new SimpleChannel();
		channel.addInterceptor(interceptor);
		channel.send(new StringMessage("test1"));
	}

	@Test
	public void testMultipleSelectorsAccept() {
		final AtomicInteger counter = new AtomicInteger();
		MessageSelector selector1 = new TestMessageSelector(true, counter);
		MessageSelector selector2 = new TestMessageSelector(true, counter);
		MessageSelectingInterceptor interceptor = new MessageSelectingInterceptor(selector1, selector2);
		SimpleChannel channel = new SimpleChannel();
		channel.addInterceptor(interceptor);
		assertTrue(channel.send(new StringMessage("test1")));
		assertEquals(2, counter.get());
	}

	@Test
	public void testMultipleSelectorsReject() {
		boolean exceptionThrown = false;
		final AtomicInteger counter = new AtomicInteger();
		MessageSelector selector1 = new TestMessageSelector(true, counter);
		MessageSelector selector2 = new TestMessageSelector(false, counter);
		MessageSelector selector3 = new TestMessageSelector(false, counter);
		MessageSelector selector4 = new TestMessageSelector(true, counter);
		MessageSelectingInterceptor interceptor = new MessageSelectingInterceptor(selector1, selector2, selector3, selector4);
		SimpleChannel channel = new SimpleChannel();
		channel.addInterceptor(interceptor);
		try {
			channel.send(new StringMessage("test1"));
		}
		catch (MessageDeliveryException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		assertEquals(2, counter.get());
	}


	private static class TestMessageSelector implements MessageSelector {

		private final boolean shouldAccept;

		private final AtomicInteger counter;


		public TestMessageSelector(boolean shouldAccept, AtomicInteger counter) {
			this.shouldAccept = shouldAccept;
			this.counter = counter;
		}


		public boolean accept(Message<?> message) {
			this.counter.incrementAndGet();
			return this.shouldAccept;
		}
	}

}
