#!/usr/bin/env groovy

/*
 * LICENSE_PLACEHOLDER
 */

/**
 * Declaratve Jenkinsfile. The language is Groovy.
 * Contains the definition of a Jenkins Pipeline, is checked into source control
 * and is expected to be the reference.
 * To fully support multibranch builds without issues, we are using docker-compose to setup cots for each build.
 *
 * @author Sylvain VISSIERE-GUERINET
 * @see https://jenkins.io/doc/book/pipeline/jenkinsfile/
 */
pipeline {
    agent any

    stages {
        stage('Deploy & Analyze') {
            when {
                anyOf {
                    branch 'master'; branch 'develop'; branch 'develop_V1.1.0'
                }
            }
            steps {
                // pour récupérer le répo local d'artefact
                // TODO: find ~jenkins/workspace/maven-repository -exec echo `pwd`/{} \; | grep -v "fr/cnes/regards" | xargs cp -R --target-directory=./test/maven-repository
                // use --exit-code-from SERVICE SERVICE to bind docker-compose return code to the one of SERVICE
                // and only launch SERVICE and its dependencies if needed
                // ${OLDPWD##*/} is the name of docker-compose.yml parent dir, -p allows us to specify container name prefix
                sh 'cd test && docker-compose -p ${OLDPWD##*/} up --exit-code-from rs_build_deploy rs_build_deploy'
            }
        }
        stage('Verify') {
            when {
                not {
                    anyOf {
                        branch 'master'; branch 'develop'; branch 'develop_V1.1.0'
                    }
                }
            }
            steps {
                sh 'cd test && docker-compose -p ${OLDPWD##*/} up --exit-code-from rs_build_verify rs_build_verify'
            }
        }
        stage('Clean') {
            steps {
                sh 'cd test && docker-compose down'
            }
        }
    }
}