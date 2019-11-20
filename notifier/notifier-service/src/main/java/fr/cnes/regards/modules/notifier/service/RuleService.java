/**
 *
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
                rules.get().map(rule -> RuleDto.build(rule.getId(), rule.getPluginCondConfiguration(), rule.isEnable(),
                                                      rule.getType()))
                        .collect(Collectors.toList()));
    }

    @Override
    public RuleDto createOrUpdateRule(@Valid RuleDto toCreate) {
        Rule toSave = Rule.build(toCreate.getId(), toCreate.getPluginConf(), toCreate.isEnabled(), toCreate.getType());
        Rule created = this.ruleRepo.save(toSave);
        return RuleDto.build(created.getId(), created.getPluginCondConfiguration(), toCreate.isEnabled(),
                             toCreate.getType());
    }

    @Override
    public void deleteRule(Long id) {
        this.ruleRepo.deleteById(id);
    }

}
