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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import org.springframework.integration.channel.MessageChannel;
import org.springframework.integration.channel.SimpleChannel;
import org.springframework.integration.message.Message;
import org.springframework.integration.message.StringMessage;

/**
 * @author Mark Fisher
 */
public class ChannelInterceptorTests {

	private final SimpleChannel channel = new SimpleChannel();


	@Test
	public void testPreSendInterceptorReturnsTrue() {
		channel.addInterceptor(new PreSendReturnsTrueInterceptor());
		channel.send(new StringMessage("test"));
		Message result = channel.receive(0);
		assertNotNull(result);
		assertEquals("test", result.getPayload());
		assertEquals(1, result.getHeader().getAttribute(PreSendReturnsTrueInterceptor.class.getName()));
	}

	@Test
	public void testPreSendInterceptorReturnsFalse() {
		channel.addInterceptor(new PreSendReturnsFalseInterceptor());
		Message message = new StringMessage("test");
		channel.send(message);
		assertEquals(1, message.getHeader().getAttribute(PreSendReturnsFalseInterceptor.class.getName()));
		Message result = channel.receive(0);
		assertNull(result);
	}

	@Test
	public void testPostSendInterceptorWithSentMessage() {
		final AtomicBoolean invoked = new AtomicBoolean(false);
		channel.addInterceptor(new ChannelInterceptorAdapter() {
			@Override
			public void postSend(Message message, MessageChannel channel, boolean sent) {
				assertNotNull(message);
				assertNotNull(channel);
				assertSame(ChannelInterceptorTests.this.channel, channel);
				assertTrue(sent);
				invoked.set(true);
			}
		});
		channel.send(new StringMessage("test"));
		assertTrue(invoked.get());
	}

	@Test
	public void testPostSendInterceptorWithUnsentMessage() {
		final AtomicInteger invokedCounter = new AtomicInteger(0);
		final AtomicInteger sentCounter = new AtomicInteger(0);
		final SimpleChannel singleItemChannel = new SimpleChannel(1);
		singleItemChannel.addInterceptor(new ChannelInterceptorAdapter() {
			@Override
			public void postSend(Message message, MessageChannel channel, boolean sent) {
				assertNotNull(message);
				assertNotNull(channel);
				assertSame(singleItemChannel, channel);
				if (sent) {
					sentCounter.incrementAndGet();
				}
				invokedCounter.incrementAndGet();
			}
		});
		assertEquals(0, invokedCounter.get());
		assertEquals(0, sentCounter.get());
		singleItemChannel.send(new StringMessage("test1"));
		assertEquals(1, invokedCounter.get());
		assertEquals(1, sentCounter.get());
		singleItemChannel.send(new StringMessage("test2"), 0);
		assertEquals(2, invokedCounter.get());
		assertEquals(1, sentCounter.get());
	}

	@Test
	public void testPreReceiveInterceptorReturnsTrue() {
		channel.addInterceptor(new PreReceiveReturnsTrueInterceptor());
		Message message = new StringMessage("test");
		channel.send(message);
		Message result = channel.receive(0);
		assertEquals(1, PreReceiveReturnsTrueInterceptor.counter.get());
		assertNotNull(result);		
	}

	@Test
	public void testPreReceiveInterceptorReturnsFalse() {
		channel.addInterceptor(new PreReceiveReturnsFalseInterceptor());
		Message message = new StringMessage("test");
		channel.send(message);
		Message result = channel.receive(0);
		assertEquals(1, PreReceiveReturnsFalseInterceptor.counter.get());
		assertNull(result);		
	}

	@Test
	public void testPostReceiveInterceptor() {
		final AtomicInteger invokedCount = new AtomicInteger();
		final AtomicInteger messageCount = new AtomicInteger();
		channel.addInterceptor(new ChannelInterceptorAdapter() {
			@Override
			public void postReceive(Message message, MessageChannel channel) {
				assertNotNull(channel);
				assertSame(ChannelInterceptorTests.this.channel, channel);
				if (message != null) {
					messageCount.incrementAndGet();
				}
				invokedCount.incrementAndGet();
			}
		});
		channel.receive(0);
		assertEquals(1, invokedCount.get());
		assertEquals(0, messageCount.get());
		channel.send(new StringMessage("test"));
		Message result = channel.receive(0);
		assertNotNull(result);		
		assertEquals(2, invokedCount.get());
		assertEquals(1, messageCount.get());
	}


	private static class PreSendReturnsTrueInterceptor extends ChannelInterceptorAdapter { 

		private static AtomicInteger counter = new AtomicInteger();

		@Override
		public boolean preSend(Message message, MessageChannel channel) {
			assertNotNull(message);
			message.getHeader().setAttribute(this.getClass().getName(), counter.incrementAndGet());
			return true;
		}
	}


	private static class PreSendReturnsFalseInterceptor extends ChannelInterceptorAdapter { 

		private static AtomicInteger counter = new AtomicInteger();

		@Override
		public boolean preSend(Message message, MessageChannel channel) {
			assertNotNull(message);
			message.getHeader().setAttribute(this.getClass().getName(), counter.incrementAndGet());
			return false;
		}
	}


	private static class PreReceiveReturnsTrueInterceptor extends ChannelInterceptorAdapter { 

		private static AtomicInteger counter = new AtomicInteger();

		@Override
		public boolean preReceive(MessageChannel channel) {
			counter.incrementAndGet();
			return true;
		}
	}


	private static class PreReceiveReturnsFalseInterceptor extends ChannelInterceptorAdapter { 

		private static AtomicInteger counter = new AtomicInteger();

		@Override
		public boolean preReceive(MessageChannel channel) {
			counter.incrementAndGet();
			return false;
		}
	}

}
