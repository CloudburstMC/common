pipeline {
    agent any

    tools {
        maven 'Maven 3'
        jdk 'Java 8'
    }

    options {
        buildDiscarder(logRotator(artifactNumToKeepStr: '1'))
    }

    stages {
        stage ('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage ('Javadoc') {
            when {
                branch "master"
            }
            steps {
                sh 'mvn javadoc:javadoc -pl api -DskipTests'
            }
            post {
                success {
                    step([
                        $class: 'JavadocArchiver',
                        javadocDir: 'api/target/site/apidocs',
                        keepAll: false
                    ])
                }
            }
        }

        stage ('Deploy') {
            when {
                branch "master"
            }
            steps {
                sh 'mvn javadoc:jar source:jar deploy -DskipTests'
            }
        }
    }
}