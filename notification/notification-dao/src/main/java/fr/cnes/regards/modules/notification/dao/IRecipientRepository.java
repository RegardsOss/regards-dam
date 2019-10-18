/**
 *
 */
package fr.cnes.regards.modules.notification.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.notification.domain.Recipient;

/**
 * @author kevin
 *
 */
@Repository
public interface IRecipientRepository extends JpaRepository<Recipient, Long> {

}
