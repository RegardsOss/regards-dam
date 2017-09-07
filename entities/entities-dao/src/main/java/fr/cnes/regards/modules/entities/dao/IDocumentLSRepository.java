package fr.cnes.regards.modules.entities.dao;

import fr.cnes.regards.modules.entities.domain.DocumentLS;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IDocumentLSRepository extends JpaRepository<DocumentLS, Long> {

    Optional<DocumentLS> findOneByDocumentAndFileChecksum(Long documentId, String fileChecksum);

}
