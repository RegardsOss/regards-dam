/**
 *
 */
package fr.cnes.regards.modules.notifier.dao;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.reguards.modules.dto.type.NotificationType;

/**
 * @author kevin
 *
 */
@Repository
public interface IRuleRepository extends JpaRepository<Rule, Long> {

    /**
     * Get all enabled {@link Rule} with the {@link NotificationType} set in parameter
     * @param type {@link NotificationType}
     * @return a set of {@link Rule}
     */
    public Set<Rule> findByEnableTrueAndType(NotificationType type);
}
