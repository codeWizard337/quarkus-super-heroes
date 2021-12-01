package io.quarkus.sample.superheroes.statistics.endpoint;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;

import io.smallrye.mutiny.Multi;

/**
 * Placeholder class to hold the {@code team-stats} in-memory channel. Main purpose is to allow mocking of the channel in tests since there isn't any way in Quarkus to mock in-memory channels.
 * <p>
 *   Also uses field injection via {@link Inject @Inject} (instead of constructor injection) to show how it is done.
 * </p>
 * @see TeamStatsWebSocket
 */
@ApplicationScoped
class TeamStatsChannelHolder {
	@Inject
	@Channel("team-stats")
	Multi<Double> teamStats;

	Multi<Double> getTeamStats() {
		return this.teamStats;
	}
}
