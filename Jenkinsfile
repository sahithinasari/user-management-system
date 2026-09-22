pipeline {

    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'checkout..'
                maven('Maven-3.9'){
                   sh 'mvn install'
                }
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
