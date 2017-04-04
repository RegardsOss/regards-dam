package fr.cnes.regards.modules.crawler.service.ds;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ExternalData associate repository
 * @author oroussel
 */
public interface ExternalDataRepository extends JpaRepository<ExternalData, Long> {

}
