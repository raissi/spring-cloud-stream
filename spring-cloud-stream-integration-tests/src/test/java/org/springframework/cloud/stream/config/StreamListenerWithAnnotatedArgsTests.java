/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.stream.config;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Marius Bogoevici
 */
public class StreamListenerWithAnnotatedArgsTests {

	@Test
	public void testInputOutputArgs() throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(TestInputOutputArgs.class, "--server.port=0");
		sendMessageAndValidate(context);
	}

	private void sendMessageAndValidate(ConfigurableApplicationContext context) throws InterruptedException {
		@SuppressWarnings("unchecked")
		Processor processor = context.getBean(Processor.class);
		processor.input().send(MessageBuilder.withPayload("hello").setHeader("contentType", "text/plain").build());
		MessageCollector messageCollector = context.getBean(MessageCollector.class);
		Message<?> result = messageCollector.forChannel(processor.output()).poll(1000, TimeUnit.MILLISECONDS);
		assertThat(result).isNotNull();
		assertThat(result.getPayload()).isEqualTo("HELLO");
		context.close();
	}


	@EnableBinding(Processor.class)
	@EnableAutoConfiguration
	public static class TestInputOutputArgs {

		@StreamListener
		public void receive(@Input(Processor.INPUT) SubscribableChannel input, @Output(Processor.OUTPUT) final MessageChannel output) {
			input.subscribe(new MessageHandler() {
				@Override
				public void handleMessage(Message<?> message) throws MessagingException {
					output.send(MessageBuilder.withPayload(message.getPayload().toString().toUpperCase()).build());
				}
			});
		}
	}

}
