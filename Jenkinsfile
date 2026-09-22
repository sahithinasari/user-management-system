pipeline {

    agent any
    tools {
        maven 'Maven-3.9'
    }
    stages {
        
        stage('Test') {
            steps {
                echo 'testing..'
                sh 'mvn test'
            }
        }

        stage('Docker Build') {
            steps {
                echo 'docker build ...'
            }
        }
    }
}
