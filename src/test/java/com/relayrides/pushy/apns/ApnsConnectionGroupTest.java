package com.relayrides.pushy.apns;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public class ApnsConnectionGroupTest extends BasePushyTest {

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testConnectAllAndDisconnectAllGracefully() throws Exception {
		final int connectionCount = 4;

		final ApnsConnectionGroup<SimpleApnsPushNotification> testGroup =
				new ApnsConnectionGroup<SimpleApnsPushNotification>(TEST_ENVIRONMENT,
						SSLTestUtil.createSSLContextForTestClient(), this.getEventLoopGroup(),
						new ApnsConnectionConfiguration(), null, "TestGroup", 4);

		final CountDownLatch connectionLatch = this.getApnsServer().getSuccessfulConnectionCountDownLatch(connectionCount);

		testGroup.connectAll();
		waitForLatch(connectionLatch);

		testGroup.disconnectAllGracefully();
		testGroup.waitForAllConnectionsToClose();
	}

	@Test
	public void testConnectAllAndDisconnectAllImmediately() throws Exception {
		final int connectionCount = 4;

		final ApnsConnectionGroup<SimpleApnsPushNotification> testGroup =
				new ApnsConnectionGroup<SimpleApnsPushNotification>(TEST_ENVIRONMENT,
						SSLTestUtil.createSSLContextForTestClient(), this.getEventLoopGroup(),
						new ApnsConnectionConfiguration(), null, "TestGroup", 4);

		final CountDownLatch connectionLatch = this.getApnsServer().getSuccessfulConnectionCountDownLatch(connectionCount);

		testGroup.connectAll();
		waitForLatch(connectionLatch);

		testGroup.disconnectAllImmediately();
		testGroup.waitForAllConnectionsToClose();
	}

	@Test
	public void testIncreaseConnectionDelay() throws Exception {
		final ApnsConnectionGroup<SimpleApnsPushNotification> testGroup =
				new ApnsConnectionGroup<SimpleApnsPushNotification>(TEST_ENVIRONMENT,
						SSLTestUtil.createSSLContextForTestClient(), this.getEventLoopGroup(),
						new ApnsConnectionConfiguration(), null, "TestGroup", 1);

		assertEquals(0, testGroup.getConnectionDelay());

		testGroup.increaseConnectionDelay();
		assertEquals(ApnsConnectionGroup.INITIAL_RECONNECT_DELAY, testGroup.getConnectionDelay());

		while (testGroup.getConnectionDelay() < ApnsConnectionGroup.MAX_RECONNECT_DELAY) {
			testGroup.increaseConnectionDelay();
		}

		testGroup.increaseConnectionDelay();
		assertEquals(ApnsConnectionGroup.MAX_RECONNECT_DELAY, testGroup.getConnectionDelay());
	}

	@Test
	public void testResetConnectionDelay() throws Exception {
		final ApnsConnectionGroup<SimpleApnsPushNotification> testGroup =
				new ApnsConnectionGroup<SimpleApnsPushNotification>(TEST_ENVIRONMENT,
						SSLTestUtil.createSSLContextForTestClient(), this.getEventLoopGroup(),
						new ApnsConnectionConfiguration(), null, "TestGroup", 1);

		assertEquals(0, testGroup.getConnectionDelay());

		testGroup.increaseConnectionDelay();
		assertTrue(testGroup.getConnectionDelay() > 0);

		testGroup.resetConnectionDelay();
		assertEquals(0, testGroup.getConnectionDelay());
	}

	@Test
	public void testWaitForAllConnectionsToCloseDate() throws Exception {
		final int connectionCount = 4;

		final ApnsConnectionGroup<SimpleApnsPushNotification> testGroup =
				new ApnsConnectionGroup<SimpleApnsPushNotification>(TEST_ENVIRONMENT,
						SSLTestUtil.createSSLContextForTestClient(), this.getEventLoopGroup(),
						new ApnsConnectionConfiguration(), null, "TestGroup", 4);

		final CountDownLatch connectionLatch = this.getApnsServer().getSuccessfulConnectionCountDownLatch(connectionCount);

		testGroup.connectAll();
		waitForLatch(connectionLatch);

		final Date deadline = new Date(System.currentTimeMillis() + 1000);

		assertFalse("Waiting for connections to close should timeout if closure has not been requested.",
				testGroup.waitForAllConnectionsToClose(deadline));

		testGroup.disconnectAllGracefully();
		testGroup.waitForAllConnectionsToClose(null);
	}

	@Test
	public void testGetNextConnection() throws Exception {
		final int connectionCount = 4;

		final ApnsConnectionGroup<SimpleApnsPushNotification> testGroup =
				new ApnsConnectionGroup<SimpleApnsPushNotification>(TEST_ENVIRONMENT,
						SSLTestUtil.createSSLContextForTestClient(), this.getEventLoopGroup(),
						new ApnsConnectionConfiguration(), null, "TestGroup", 4);

		final CountDownLatch connectionLatch = this.getApnsServer().getSuccessfulConnectionCountDownLatch(connectionCount);

		testGroup.connectAll();
		waitForLatch(connectionLatch);

		final ApnsConnection<SimpleApnsPushNotification> firstConnection = testGroup.getNextConnection();
		final ApnsConnection<SimpleApnsPushNotification> secondConnection = testGroup.getNextConnection();

		assertNotNull(firstConnection);
		assertNotNull(secondConnection);
		assertNotEquals(firstConnection, secondConnection);

		testGroup.disconnectAllGracefully();
		testGroup.waitForAllConnectionsToClose(null);
	}

	@Test
	public void testGetNextConnectionLong() throws Exception {
		final int connectionCount = 4;

		final ApnsConnectionGroup<SimpleApnsPushNotification> testGroup =
				new ApnsConnectionGroup<SimpleApnsPushNotification>(TEST_ENVIRONMENT,
						SSLTestUtil.createSSLContextForTestClient(), this.getEventLoopGroup(),
						new ApnsConnectionConfiguration(), null, "TestGroup", 4);

		final long timeoutMillis = 1000;
		assertNull(testGroup.getNextConnection(timeoutMillis));

		final CountDownLatch connectionLatch = this.getApnsServer().getSuccessfulConnectionCountDownLatch(connectionCount);

		testGroup.connectAll();
		waitForLatch(connectionLatch);

		assertNotNull(testGroup.getNextConnection(timeoutMillis));

		testGroup.disconnectAllGracefully();
		testGroup.waitForAllConnectionsToClose(null);
	}
}
