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

import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.notifier.dao.IRuleRepository;
import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.reguards.modules.notifier.dto.RuleDto;

/**
 * @author kevin
 *
 */
@Service
@MultitenantTransactional
public class RuleService implements IRuleService {

    @Autowired
    private IRuleRepository ruleRepo;

    @Override
    public Page<RuleDto> getRules(Pageable page) {
        Page<Rule> rules = ruleRepo.findAll(page);
        return new PageImpl<>(
                rules.get().map(rule -> RuleDto.build(rule.getId(), rule.getRulePlugin(), rule.isEnable(),
                                                      rule.getType()))
                        .collect(Collectors.toList()));
    }

    @Override
    public RuleDto createOrUpdateRule(@Valid RuleDto toCreate) {
        Rule toSave = Rule.build(toCreate.getId(), toCreate.getPluginConf(), toCreate.isEnabled(), toCreate.getType());
        Rule created = this.ruleRepo.save(toSave);
        return RuleDto.build(created.getId(), created.getRulePlugin(), toCreate.isEnabled(),
                             toCreate.getType());
    }

    @Override
    public void deleteRule(Long id) {
        this.ruleRepo.deleteById(id);
    }

}
