/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.plugintypes;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;


/**
 * @author Christophe Mertz
 *
 */
public abstract class AbstractDataSourcePlugin implements IDataSourcePlugin {

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.datasources.plugins.IEntityPagingAndSortingRepository#findBy(org.hibernate.criterion.Restrictions)
     */
    @Override
    public List<AbstractEntity> findBy(Restrictions pCondition) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.datasources.plugins.IEntityPagingAndSortingRepository#findBy(org.hibernate.criterion.Restrictions, org.springframework.data.domain.Sort)
     */
    @Override
    public List<AbstractEntity> findBy(Restrictions pCondition, Sort pSort) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.datasources.plugins.IEntityPagingAndSortingRepository#findBy(org.hibernate.criterion.Restrictions, org.springframework.data.domain.Pageable)
     */
    @Override
    public Page<AbstractEntity> findBy(Restrictions pCondition, Pageable pPageable) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(org.springframework.data.domain.Sort)
     */
    @Override
    public Iterable<AbstractEntity> findAll(Sort sort) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(org.springframework.data.domain.Pageable)
     */
    @Override
    public Page<AbstractEntity> findAll(Pageable pageable) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#save(java.lang.Object)
     */
    @Override
    public <S extends AbstractEntity> S save(S entity) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#save(java.lang.Iterable)
     */
    @Override
    public <S extends AbstractEntity> Iterable<S> save(Iterable<S> entities) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findOne(java.io.Serializable)
     */
    @Override
    public AbstractEntity findOne(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#exists(java.io.Serializable)
     */
    @Override
    public boolean exists(Long id) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll()
     */
    @Override
    public Iterable<AbstractEntity> findAll() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll(java.lang.Iterable)
     */
    @Override
    public Iterable<AbstractEntity> findAll(Iterable<Long> ids) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#count()
     */
    @Override
    public long count() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#delete(java.io.Serializable)
     */
    @Override
    public void delete(Long id) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Object)
     */
    @Override
    public void delete(AbstractEntity entity) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Iterable)
     */
    @Override
    public void delete(Iterable<? extends AbstractEntity> entities) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#deleteAll()
     */
    @Override
    public void deleteAll() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor#findOne(org.springframework.data.jpa.domain.Specification)
     */
    @Override
    public AbstractEntity findOne(Specification<AbstractEntity> spec) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor#findAll(org.springframework.data.jpa.domain.Specification)
     */
    @Override
    public List<AbstractEntity> findAll(Specification<AbstractEntity> spec) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor#findAll(org.springframework.data.jpa.domain.Specification, org.springframework.data.domain.Pageable)
     */
    @Override
    public Page<AbstractEntity> findAll(Specification<AbstractEntity> spec, Pageable pageable) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor#findAll(org.springframework.data.jpa.domain.Specification, org.springframework.data.domain.Sort)
     */
    @Override
    public List<AbstractEntity> findAll(Specification<AbstractEntity> spec, Sort sort) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor#count(org.springframework.data.jpa.domain.Specification)
     */
    @Override
    public long count(Specification<AbstractEntity> spec) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.datasources.plugins.IDataSourcePlugin#getRefreshRate()
     */
    @Override
    public int getRefreshRate() {
        // TODO Auto-generated method stub
        return 0;
    }

}
