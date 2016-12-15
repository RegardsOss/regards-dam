/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.rest;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.cloud.netflix.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fr.cnes.regards.client.core.FeignClientConfiguration;
import fr.cnes.regards.modules.accessrights.client.IProjectUsersClient;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.dataaccess.service.FeignConfiguration;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Configuration
@EnableAutoConfiguration(
        exclude = { FeignAutoConfiguration.class, FeignClientConfiguration.class, FeignClientsConfiguration.class })
@ComponentScan(basePackages = "fr.cnes.regards.modules", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = FeignConfiguration.class) })
public class MockedFeignConfiguration {

    @Bean
    public IProjectUsersClient projectUserClient() {
        // MockClient mockClient = new MockClient().ok(HttpMethod.GET, "/users/user1@user1.user1", null);
        // return Feign.builder().client(mockClient).target(new MockTarget<>(IProjectUsersClient.class));
        return new IProjectUsersClient() {

            @Override
            public ResponseEntity<PagedResources<Resource<ProjectUser>>> retrieveProjectUserList(int pPage, int pSize) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ResponseEntity<PagedResources<Resource<ProjectUser>>> retrieveAccessRequestList(int pPage,
                    int pSize) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ResponseEntity<Resource<ProjectUser>> retrieveProjectUser(String pUserEmail) {
                return new ResponseEntity<>(new Resource<>(new ProjectUser()), HttpStatus.OK);
            }

            @Override
            public ResponseEntity<Resource<ProjectUser>> updateProjectUser(Long pUserId,
                    ProjectUser pUpdatedProjectUser) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ResponseEntity<Void> removeProjectUser(Long pUserId) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ResponseEntity<PagedResources<Resource<ProjectUser>>> retrieveRoleProjectUserList(Long pRoleId,
                    int pPage, int pSize) {
                // TODO Auto-generated method stub
                return null;
            }

        };
    }

}
