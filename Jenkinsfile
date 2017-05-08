node {
    def paaswordSpvImage

    stage('Clone') {
        checkout scm
    }

    stage('Build') {
        paaswordSpvImage = docker.build("chrispetsos/paasword-spv")
    }

    stage('Test') {
        // TODO: Run tests on image

        paaswordSpvImage.inside {
            sh 'echo "TODO: Run tests on image"'
        }
    }

    stage('Deploy') {
        docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
            paaswordSpvImage.push("latest")
        }
    }
}