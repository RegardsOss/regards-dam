/**
 *
 */
package fr.cnes.regards.modules.notification.service;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.notification.domain.Recipient;
import fr.cnes.reguards.modules.dto.RecipientDto;

/**
 * @author kevin
 *
 */
public interface IRecipientService {

    public Page<RecipientDto> getRecipients(Pageable page);

    /**
     * Create or update a {@link Recipient} from a {@link RecipientDto}
     * @param toCreate
     * @return {@link RecipientDto} from the created {@link Recipient}
     */
    public RecipientDto createOrUpdateRecipient(@Valid RecipientDto toCreate);

    /**
     * Delete a {@link Recipient} by its id
     * @param id
     */
    public void deleteRecipient(Long id);
}
