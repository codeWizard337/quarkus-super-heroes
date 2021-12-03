package io.quarkus.sample.superheroes.statistics.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Tests for the {@link TeamStatsWebSocket} class.
 * <p>
 *   These tests mock the {@link TeamStatsChannelHolder#getTeamStats()} method to return pre-defined input and then set up a sample WebSocket client to listen to messages sent by the server. Each message received is placed into a {@link BlockingQueue} so that message content can be asserted once the expected number of messages have been received.
 * </p>
 */
@QuarkusTest
class TeamStatsWebSocketTests {
	@TestHTTPResource("/stats/team")
	URI uri;

	@InjectMock
	TeamStatsChannelHolder teamStatsChannelHolder;

	@Test
	public void teamStatsWebSocketTestScenario() throws DeploymentException, IOException, InterruptedException {
		// Set up the Queue to handle the messages
		var messages = new LinkedBlockingQueue<String>();
		
		// Set up a single consumer latch
		// It will wait for the client to connect and subscribe to the stream before emitting items
		var latch = new CountDownLatch(1);
		var delayedUni = Uni.createFrom().voidItem()
			.onItem().delayIt().until(x -> {
				try {
					latch.await();
					return Uni.createFrom().nullItem();
				}
				catch (InterruptedException ex) {
					return Uni.createFrom().failure(ex);
				}
			});

		// Delay the emission of the Multi until the client subscribes
		var delayedItemsMulti = Multi.createFrom().items(TeamStatsWebSocketTests::createItems)
			.onItem().call(items -> Uni.createFrom().nullItem().onItem().delayIt().until(o -> delayedUni));

		// Mock TeamStatsChannelHolder.getTeamStats() to return the delayed Multi
		when(this.teamStatsChannelHolder.getTeamStats()).thenReturn(delayedItemsMulti);

		// Set up the client to connect to the socket
		try (var session = ContainerProvider.getWebSocketContainer().connectToServer(new EndpointTestClient(messages), this.uri)) {
			// Make sure client connected
			assertThat(messages.poll(5, TimeUnit.MINUTES))
				.isNotNull()
				.isEqualTo("CONNECT");

			// Client has connected - trigger the Multi subscription
			latch.countDown();

			var expectedItems = createItems()
				.map(String::valueOf)
				.collect(Collectors.toList());

			// Wait for our messages to appear in the queue
			await()
				.atMost(Duration.ofMinutes(5))
				.until(() -> messages.size() == expectedItems.size());

			System.out.println("Messages received by test: " + messages);

			// Perform assertions that all expected messages were received
			assertThat(messages)
				.containsExactlyElementsOf(expectedItems);
		}
	}

	private static Stream<Double> createItems() {
		return DoubleStream.of(0.0, 0.25, 0.5, 0.75, 1.0).boxed();
	}

	@ClientEndpoint
	private class EndpointTestClient {
		private final Logger logger = Logger.getLogger(EndpointTestClient.class);
		private final BlockingQueue<String> messages;

		private EndpointTestClient(BlockingQueue<String> messages) {
			this.messages = messages;
		}

		@OnOpen
		public void open() {
			this.logger.info("Opening socket");
			this.messages.offer("CONNECT");
		}

		@OnMessage
		public void message(String msg) {
			this.logger.infof("Got message: %s", msg);
			this.messages.offer(msg);
		}

		@OnClose
		public void close(CloseReason closeReason) {
			this.logger.infof("Closing socket: %s", closeReason);
		}

		@OnError
		public void error(Throwable error) {
			this.logger.errorf(error, "Socket encountered error");
		}
	}
}
