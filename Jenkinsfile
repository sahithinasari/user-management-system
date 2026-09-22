pipeline {

    agent any
    tools {
        maven 'Maven-3.9'
    }
    stages {
        stage('Checkout') {
            steps {
                echo 'checkout..'
                sh 'mvn install'
            }
        }

        stage('Test') {
            steps {
                echo 'testing..'
            }
        }

        stage('Docker Build') {
            steps {
                echo 'docker build ...'
            }
        }
    }
}
