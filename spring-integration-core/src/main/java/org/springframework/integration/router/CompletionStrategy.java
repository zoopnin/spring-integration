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

package org.springframework.integration.router;

import java.util.List;

import org.springframework.integration.message.Message;

/**
 * Strategy for determining when a group of messages reaches a state of
 * completion (i.e. can trip a barrier).
 * 
 * @author Mark Fisher
 * @see AggregationBarrier
 */
public interface CompletionStrategy {

	boolean isComplete(List<Message<?>> messages);

}
