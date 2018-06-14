pipeline {
    agent {
        docker {
            image 'gradlewrapper:latest'
            args '-v gradlecache:/gradlecache'
        }
    }
    environment {
        GRADLE_ARGS = '-Dorg.gradle.daemon.idletimeout=5000'
    }

    stages {
        stage('fetch') {
            steps {
                git(url: 'https://github.com/cpw/accesstransformers.git', changelog: true)
            }
        }
        stage('buildandtest') {
            steps {
                sh './gradlew ${GRADLE_ARGS} --refresh-dependencies --continue build test'
            }
            post {
                success {
                    script {
                        def changelogString = gitChangelog noIssueName: 'Unspecified',
                        returnType: 'STRING',
                        template: '''
                        <h1>Access Transformers Changelog</h1>

                        <p>
                        Changelog of Access Transformers library.
                        </p>

                        {{#tags}}
                        <h2> {{name}} </h2>
                         {{#issues}}
                          {{#hasIssue}}
                           {{#hasLink}}
                        <h2> {{name}} <a href="{{link}}">{{issue}}</a> {{title}} </h2>
                           {{/hasLink}}
                           {{^hasLink}}
                        <h2> {{name}} {{issue}} {{title}} </h2>
                           {{/hasLink}}
                          {{/hasIssue}}
                          {{^hasIssue}}
                        <h2> {{name}} </h2>
                          {{/hasIssue}}


                           {{#commits}}
                        <a href="https://github.com/cpw/accesstransformers/commit/{{hash}}">{{hash}}</a> {{authorName}} <i>{{commitTime}}</i>
                        <p>
                        <h3>{{{messageTitle}}}</h3>

                        {{#messageBodyItems}}
                         <li> {{.}}</li>
                        {{/messageBodyItems}}
                        </p>


                          {{/commits}}

                         {{/issues}}
                        {{/tags}}
                        '''
                        writeFile file: "build/changelog.html", text: "${changelogString}"
                    }
                }
            }
        }
        stage('publish') {
            when {
                branch 'master'
            }
            environment {
                FORGE_MAVEN = credentials('forge-maven-cpw-user')
            }
            steps {
                sh './gradlew ${GRADLE_ARGS} publish -PforgeMavenUser=${FORGE_MAVEN_USR} -PforgeMavenPassword=${FORGE_MAVEN_PSW}'
                sh 'curl --user ${FORGE_MAVEN} http://files.minecraftforge.net/maven/manage/promote/latest/cpw.mods.accesstransformers/${BUILD_NUMBER}'
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'build/changelog.html', fingerprint: false
            archiveArtifacts artifacts: 'build/libs/**/*.jar', fingerprint: true
            junit 'build/test-results/*/*.xml'
            jacoco sourcePattern: '**/src/*/java'
        }
    }
}
