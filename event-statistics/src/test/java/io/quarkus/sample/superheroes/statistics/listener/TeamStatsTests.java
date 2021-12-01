package io.quarkus.sample.superheroes.statistics.listener;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.sample.superheroes.statistics.domain.Fight;

/**
 * Tests for the {@link io.quarkus.sample.superheroes.statistics.listener.TeamStats} class. Not a {@link io.quarkus.test.junit.QuarkusTest @QuarkusTest} because the test can simply call the methods with the appropriate input.
 */
class TeamStatsTests {
	private static final Fight HERO_WINNER = Fight.builder().winnerTeam("heroes").build();
	private static final Fight VILLAIN_WINNER = Fight.builder().winnerTeam("villains").build();
	
	private TeamStats teamStats = new TeamStats();
	
	@Test
	public void teamStatsScenario() {
		assertThat(this.teamStats.add(HERO_WINNER))
			.isEqualTo((double) 1/1);

		assertThat(this.teamStats.add(VILLAIN_WINNER))
			.isEqualTo((double) 1/2);

		assertThat(this.teamStats.add(HERO_WINNER))
			.isEqualTo((double) 2/3);

		assertThat(this.teamStats.add(VILLAIN_WINNER))
			.isEqualTo((double) 2/4);

		assertThat(this.teamStats.add(HERO_WINNER))
			.isEqualTo((double) 3/5);

		assertThat(this.teamStats.add(VILLAIN_WINNER))
			.isEqualTo((double) 3/6);

		assertThat(this.teamStats.add(HERO_WINNER))
			.isEqualTo((double) 4/7);

		assertThat(this.teamStats.add(VILLAIN_WINNER))
			.isEqualTo((double) 4/8);

		assertThat(this.teamStats.add(HERO_WINNER))
			.isEqualTo((double) 5/9);

		assertThat(this.teamStats.add(VILLAIN_WINNER))
			.isEqualTo((double) 5/10);

		assertThat(this.teamStats.getHeroesCount())
			.isEqualTo(5);

		assertThat(this.teamStats.getVillainsCount())
			.isEqualTo(5);
	}
}
