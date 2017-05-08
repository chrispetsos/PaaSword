node {
    def paasword-spv-image

    stage('Clone') {
        checkout scm
    }

    stage('Build') {
        paasword-spv-image = docker.build("chrispetsos/paasword-spv")
    }

    stage('Test') {
        // TODO: Run tests on image

        paasword-spv-image.inside {
            sh 'echo "TODO: Run tests on image"'
        }
    }

    stage('Deploy') {
        docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
            paasword-spv-image.push("latest")
        }
    }
}