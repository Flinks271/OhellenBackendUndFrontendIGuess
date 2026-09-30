package de.ohellen.demo.infrastructure.persistence;

import de.ohellen.demo.domain.game.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameResultRepository extends JpaRepository<GameResult, Long> {
}
