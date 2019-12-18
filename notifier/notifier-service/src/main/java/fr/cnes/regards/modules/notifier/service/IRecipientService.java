/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.notifier.service;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.reguards.modules.notifier.dto.RecipientDto;

/**
 * Service for {@link Recipient} manipulation
 * @author Kevin Marchois
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
