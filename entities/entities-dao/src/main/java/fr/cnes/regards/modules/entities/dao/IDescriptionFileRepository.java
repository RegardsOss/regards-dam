package fr.cnes.regards.modules.entities.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.entities.domain.DescriptionFile;

/**
 * @author Sylvain VISSIERE-GUERINET
 */
public interface IDescriptionFileRepository extends JpaRepository<DescriptionFile, Long> {

}
