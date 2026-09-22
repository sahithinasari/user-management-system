pipeline {

    agent any

    stages {

        tools {
            maven 'Maven-3.9'
        }

        stage('Checkout') {
            steps {
                echo 'checkout..'
                sh 'mvn test'
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
