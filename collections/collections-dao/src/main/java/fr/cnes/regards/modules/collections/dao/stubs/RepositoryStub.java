/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.dao.stubs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.hateoas.Identifiable;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
@Primary
public class RepositoryStub<T extends Identifiable<String>> implements CrudRepository<T, String> {

    protected Set<T> entities_ = new HashSet<>();

    @Override
    public <S extends T> S save(S pEntity) {
        entities_.removeIf(r -> r.equals(pEntity));
        entities_.add(pEntity);
        return pEntity;
    }

    @Override
    public T findOne(String pId) {
        return entities_.stream().filter(r -> r.getId().equals(pId)).findFirst().get();
    }

    @Override
    public boolean exists(String pId) {
        return entities_.stream().filter(r -> r.getId().equals(pId)).findAny().isPresent();
    }

    @Override
    public long count() {
        return entities_.size();
    }

    @Override
    public void delete(String pId) {
        entities_.removeIf(r -> r.getId().equals(pId));
    }

    @Override
    public void delete(T pEntity) {
        entities_.remove(pEntity);
    }

    @Override
    public void delete(Iterable<? extends T> pEntities) {
        for (T entity : pEntities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        entities_ = new HashSet<>();
    }

    @Override
    public <S extends T> List<S> save(Iterable<S> pEntities) {
        List<S> savedEntities = new ArrayList<>();

        for (S entity : pEntities) {
            savedEntities.add(save(entity));
        }

        return savedEntities;
    }

    @Override
    public Iterable<T> findAll() {
        return entities_;
    }

    @Override
    public Iterable<T> findAll(Iterable<String> pIds) {
        return StreamSupport.stream(pIds.spliterator(), false).map(id -> findOne(id)).collect(Collectors.toList());
    }

    /**
     * @return the entities
     */
    public Set<T> getEntities() {
        return entities_;
    }

}
