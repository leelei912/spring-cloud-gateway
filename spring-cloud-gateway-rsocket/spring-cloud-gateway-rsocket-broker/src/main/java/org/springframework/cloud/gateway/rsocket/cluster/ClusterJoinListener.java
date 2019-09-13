/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gateway.rsocket.cluster;

import java.math.BigInteger;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.rsocket.actuate.BrokerActuator;
import org.springframework.cloud.gateway.rsocket.actuate.BrokerInfo;
import org.springframework.cloud.gateway.rsocket.autoconfigure.BrokerProperties;
import org.springframework.cloud.gateway.rsocket.common.autoconfigure.Broker;
import org.springframework.cloud.gateway.rsocket.common.metadata.Forwarding;
import org.springframework.cloud.gateway.rsocket.common.metadata.RouteSetup;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;

public class ClusterJoinListener implements ApplicationListener<ApplicationReadyEvent> {

	private final ClusterService clusterService;

	private final BrokerProperties properties;

	private final RSocketStrategies strategies;

	public ClusterJoinListener(ClusterService clusterService, BrokerProperties properties,
			RSocketStrategies strategies) {
		this.clusterService = clusterService;
		this.properties = properties;
		this.strategies = strategies;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		for (Broker broker : properties.getBrokers()) {
			RouteSetup.Builder routeSetup = RouteSetup.of(properties.getRouteId(),
					properties.getServiceName());

			// TODO: micrometer
			RSocketRequester.builder().rsocketStrategies(strategies)
					.setupMetadata(routeSetup.build(), RouteSetup.ROUTE_SETUP_MIME_TYPE)
					// TODO: other types
					.connectTcp(broker.getHost(), broker.getPort())
					.flatMap(this::callBrokerInfo).subscribe(this::registerOutgoing);
		}
	}

	Mono<Tuple2<BigInteger, RSocketRequester>> callBrokerInfo(
			RSocketRequester requester) {
		Forwarding forwarding = Forwarding.of(properties.getRouteId())
				.serviceName("gateway").disableProxy().build();
		requester.route(BrokerActuator.BROKER_INFO_PATH)
				.metadata(forwarding, Forwarding.FORWARDING_MIME_TYPE)
				.data(BrokerInfo.of(properties.getRouteId()).build())
				.retrieveMono(BigInteger.class)
				.map(brokerId -> Tuples.of(brokerId, requester));
	}

	boolean registerOutgoing(Tuple2<BigInteger, RSocketRequester> tuple) {
		return clusterService.registerOutgoing(tuple.getT1().toString(), tuple.getT2());
	}

}
