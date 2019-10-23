/**
 *
 */
package fr.cnes.regards.modules.notification.service;

import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.notification.dao.IRecipientRepository;
import fr.cnes.regards.modules.notification.domain.Recipient;
import fr.cnes.regards.modules.notification.domain.Rule;
import fr.cnes.reguards.modules.dto.RecipientDto;
import fr.cnes.reguards.modules.dto.RuleDto;

/**
 * @author kevin
 *
 */
public class RecipientService implements IRecipientService {

    @Autowired
    private IRecipientRepository recipientRepo;

    @Override
    public Page<RecipientDto> getRecipients(Pageable page) {
        Page<Recipient> recipients = recipientRepo.findAll(page);
        return new PageImpl<>(recipients.get()
                .map(recipient -> RecipientDto.build(recipient.getId(), intiRuleDto(recipient),
                                                     recipient.getPluginCondConfiguration()))
                .collect(Collectors.toList()));
    }

    private RuleDto intiRuleDto(Recipient recipient) {
        Rule rule = recipient.getRule();
        return rule == null ? null
                : RuleDto.build(rule.getId(), rule.getPluginCondConfiguration(), rule.isEnable(), rule.getType());
    }

    @Override
    public RecipientDto createOrUpdateRecipient(@Valid RecipientDto toCreate) {
        RuleDto rule = toCreate.getRule();
        Recipient toSave = rule == null ? Recipient.build(null, toCreate.getPluginConf())
                : Recipient.build(Rule.build(rule.getId(), rule.getPluginConf(), rule.isEnabled(), rule.getType()),
                                  toCreate.getPluginConf());
        Recipient created = this.recipientRepo.save(toSave);
        return RecipientDto.build(created.getId(), intiRuleDto(created), created.getPluginCondConfiguration());
    }

    @Override
    public void deleteRecipient(Long id) {
        this.recipientRepo.deleteById(id);
    }

}
