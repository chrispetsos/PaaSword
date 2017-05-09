node {
    def paaswordSpvImage

    stage('Clone') {
        checkout scm
    }

    stage('Test') {
    	sh 'mvn clean test'
    }

    stage('Build') {
    	sh 'mvn package -DskipTests=true'
        paaswordSpvImage = docker.build("chrispetsos/paasword-spv")
    }

    stage('Deploy') {
        docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
            paaswordSpvImage.push("latest")
        }
    }
}