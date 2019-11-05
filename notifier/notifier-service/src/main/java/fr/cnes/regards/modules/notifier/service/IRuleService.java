/**
 *
 */
package fr.cnes.regards.modules.notifier.service;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.reguards.modules.notifier.dto.RuleDto;

/**
 * @author kevin
 *
 */
public interface IRuleService {

    public Page<RuleDto> getRules(Pageable page);

    /**
     * Create or update a {@link Rule} from a {@link RuleDto}
     * @param toCreate
     * @return {@link RuleDto} from the created {@link Rule}
     */
    public RuleDto createOrUpdateRule(@Valid RuleDto toCreate);

    /**
     * Delete a {@link Rule} by its id
     * @param id
     */
    public void deleteRule(Long id);
}
