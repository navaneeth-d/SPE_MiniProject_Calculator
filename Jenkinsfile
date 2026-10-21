pipeline {
    agent any

    environment {
        DOCKERHUB_REPOSITORY = 'ar3snd/calculator_spe'
        IMAGE_NAME           = "${DOCKERHUB_REPOSITORY}:v${BUILD_NUMBER}"
        LATEST_IMAGE         = "${DOCKERHUB_REPOSITORY}:latest"
        DOCKERHUB_CREDS_ID   = 'dockerhub_credentials'
        NOTIFICATION_EMAIL   = credentials('notification_email')        //  use Jenkins credentials for storing email id
        HOST_PORT            = '8081'
        COMPOSE_PROJECT_NAME = 'calculator'
        // Configured PATH and JAVA_HOME inside Jenkins->Node
    }

    options {
        skipDefaultCheckout(true)          // we check out once, explicitly, below
        timestamps()
        disableConcurrentBuilds()          // two deploys must never race
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '5'))
        timeout(time: 30, unit: 'MINUTES')
    }

    triggers {
        githubPush()                       // GitHub webhook (via ngrok)
    }

    stages {
        stage('Preflight') {
            steps {
                sh '''
                    set -eu
                    echo "--- Java ---";    java -version
                    echo "--- Maven ---";   ./mvnw -version
                    echo "--- Docker ---";  docker --version
                    docker info > /dev/null 2>&1 || { 
                        echo "ERROR: Docker daemon not reachable. Start Docker Desktop."; exit 1; 
                    }
                    echo "--- Compose ---"; docker compose version
                    docker compose config --quiet
                    echo "Preflight OK"
                '''
            }
        }

        stage('Checkout') {
            steps {
                script {
                    def scmVars = checkout scm
                    env.GIT_COMMIT_SHORT = scmVars.GIT_COMMIT.take(7)
                    env.GIT_COMMIT_MSG = sh(returnStdout: true, script: 'git log -1 --pretty=%s').trim()
                }
                echo "Commit ${env.GIT_COMMIT_SHORT}: ${env.GIT_COMMIT_MSG}"
            }
        }

        stage('Maven Test') {
            steps {
                sh './mvnw -B -ntp test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/TEST-*.xml', allowEmptyResults: false
                }
            }
        }

        stage('Maven Build') {
            steps {
                sh './mvnw -B -ntp clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: false
                }
            }
        }
    
        stage('Coverage Report') {
            steps {
                sh './mvnw -B -ntp jacoco:report'

                recordCoverage(
                    tools: [[
                        parser: 'JACOCO',
                        pattern: 'target/site/jacoco/jacoco.xml'
                    ]]
                )
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                        --label "org.opencontainers.image.revision=${GIT_COMMIT_SHORT}" \
                        --label "org.opencontainers.image.version=${BUILD_NUMBER}" \
                        --tag "${IMAGE_NAME}" \
                        --tag "${LATEST_IMAGE}" \
                        .
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.DOCKERHUB_CREDS_ID,
                    usernameVariable: 'DH_USER',
                    passwordVariable: 'DH_PASS'
                )]) {
                    sh '''
                        set -eu

                        # Create an isolated Docker credential configuration.
                        DOCKER_CONFIG="$(mktemp -d)"
                        export DOCKER_CONFIG

                        # Always remove the temporary credentials, even on failure.
                        trap 'rm -rf "$DOCKER_CONFIG"' EXIT

                        # Authenticate using credentials provided in Jenkins, only in this temporary config.
                        echo "$DH_PASS" | docker login \
                            --username "$DH_USER" \
                            --password-stdin

                        # Push the image.
                        docker push "$IMAGE_NAME"

                        echo "Docker image pushed successfully."
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    set -eu

                    # Remember what is running now, so we can roll back if the new one is bad.
                    PREV_IMAGE=""
                    CID="$(docker compose ps -q calculator | head -n1 || true)"
                    if [ -n "$CID" ]; then
                        PREV_IMAGE="$(docker inspect --format '{{.Config.Image}}' "$CID")"
                    fi
                    echo "Previous image: ${PREV_IMAGE:-none}"
                    echo "New image:      ${IMAGE_NAME}"

                    rollback() {
                        echo "!!! Deployment failed"
                        docker compose ps || true
                        docker compose logs --tail=100 || true
                        if [ -n "$PREV_IMAGE" ] && [ "$PREV_IMAGE" != "$IMAGE_NAME" ]; then
                            echo "Rolling back to $PREV_IMAGE"
                            IMAGE_NAME="$PREV_IMAGE" docker compose up -d --remove-orphans --wait --wait-timeout 120 || true
                        fi
                        exit 1
                    }

                    # up -d is idempotent: unchanged services are left alone, changed ones are recreated.
                    docker compose pull         || rollback
                    docker compose up -d --remove-orphans --wait --wait-timeout 120 || rollback

                    # Verify through nginx (the real entry point), not just the container.
                    BASE="http://localhost:${HOST_PORT}"
                    verify() {
                        curl -fsS --max-time 5 "$BASE/actuator/health" | grep -q '"status" *: *"UP"' &&
                        curl -fsS --max-time 5 "$BASE/api/v1/add?a=2&b=3" | grep -Eq '"result" *: *5(\\.0)?[,}]' &&
                        [ "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/v1/divide?a=1&b=0")" = "400" ]
                    }

                    ok=0
                    for attempt in $(seq 1 30); do
                        if verify; then ok=1; break; fi
                        echo "Waiting for stack ($attempt/30)..."
                        sleep 2
                    done
                    [ "$ok" = "1" ] || rollback

                    echo "Deployment verified: health UP, add=5, divide-by-zero=400."
                    docker compose ps
                '''
            }
        }
    }

    post {
        always {
            // Remove dangling image layers only.
            // Does not stop or remove running containers.
            sh 'docker image prune -f || true'
        }

        success {
            mail(
                to: env.NOTIFICATION_EMAIL,
                subject: "[SUCCESS] ${env.JOB_NAME} #${env.BUILD_NUMBER} - Pipeline Completed",
                mimeType: 'text/html',
                body: """
                <html>
                <body style="font-family:Arial,Helvetica,sans-serif; color:#24292f; line-height:1.6; max-width:700px; margin:auto;">

                    <div style="background:#e6f4ea; padding:20px; border-radius:8px; border-left:5px solid #188038;">
                        <h2 style="margin:0; color:#188038;">&#10004; Pipeline Succeeded</h2>
                        <p style="margin:6px 0 0;">Your calculator application was built, tested, and deployed successfully.</p>
                    </div>

                    <h3>Build Summary</h3>
                    <table style="border-collapse:collapse; width:100%; font-size:14px;">
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Project</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de;">${env.JOB_NAME}</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Build Number</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de;">#${env.BUILD_NUMBER}</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Status</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de; color:#188038;"><b>SUCCESS</b></td>
                        </tr>
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Duration</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de;">${currentBuild.durationString}</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Git Commit</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de;">${env.GIT_COMMIT_SHORT ?: 'n/a'}</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Commit Message</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de;">${env.GIT_COMMIT_MSG ?: 'Not available'}</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Docker Image</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de;">${env.IMAGE_NAME ?: 'Not available'}</td>
                        </tr>
                    </table>

                    <h3>Pipeline Results</h3>
                    <p>
                        The pipeline completed successfully. Build, tests, container image publishing,
                        deployment, and configured health checks completed without a reported failure.
                    </p>

                    <div style="margin:24px 0;">
                        <a href="${env.BUILD_URL}"
                        style="display:inline-block; padding:12px 18px; background:#0969da; color:#ffffff; text-decoration:none; border-radius:5px; font-weight:bold;">
                        View Build Details
                        </a>
                        &nbsp;
                        <a href="${env.BUILD_URL}pipeline/"
                        style="display:inline-block; padding:12px 18px; background:#57606a; color:#ffffff; text-decoration:none; border-radius:5px; font-weight:bold;">
                        View Pipeline Stages
                        </a>
                    </div>

                    <p style="font-size:12px; color:#57606a;">
                        Build URL: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a>
                    </p>

                    <hr style="border:none; border-top:1px solid #d8dee4; margin-top:28px;"/>
                    <p style="font-size:12px; color:#57606a;">
                        Automated notification from Jenkins CI/CD.<br/>
                        Please do not reply to this email.
                    </p>

                </body>
                </html>
                """
            )
        }

        failure {
            mail(
                to: env.NOTIFICATION_EMAIL,
                subject: "[FAILURE] ${env.JOB_NAME} #${env.BUILD_NUMBER} - Action Required",
                mimeType: 'text/html',
                body: """
                <html>
                <body style="font-family:Arial,Helvetica,sans-serif; color:#24292f; line-height:1.6; max-width:700px; margin:auto;">

                    <div style="background:#ffebe9; padding:20px; border-radius:8px; border-left:5px solid #cf222e;">
                        <h2 style="margin:0; color:#cf222e;">&#10006; Pipeline Failed</h2>
                        <p style="margin:6px 0 0;">A pipeline stage failed. Please inspect the build logs before retrying deployment.</p>
                    </div>

                    <h3>Failure Summary</h3>
                    <table style="border-collapse:collapse; width:100%; font-size:14px;">
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Project</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de;">${env.JOB_NAME}</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Build Number</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de;">#${env.BUILD_NUMBER}</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Status</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de; color:#cf222e;"><b>FAILURE</b></td>
                        </tr>
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Duration</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de;">${currentBuild.durationString}</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Git Commit</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de;">${env.GIT_COMMIT_SHORT ?: 'n/a'}</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; background:#f6f8fa; border:1px solid #d0d7de;"><b>Commit Message</b></td>
                            <td style="padding:10px; border:1px solid #d0d7de;">${env.GIT_COMMIT_MSG ?: 'Not available'}</td>
                        </tr>
                    </table>

                    <h3>Recommended Next Steps</h3>
                    <ol>
                        <li>Open the pipeline stage view to identify the failed stage.</li>
                        <li>Inspect the console output for the exact error.</li>
                        <li>Fix the issue and rerun the pipeline.</li>
                    </ol>

                    <div style="margin:24px 0;">
                        <a href="${env.BUILD_URL}console"
                        style="display:inline-block; padding:12px 18px; background:#cf222e; color:#ffffff; text-decoration:none; border-radius:5px; font-weight:bold;">
                        Inspect Console Log
                        </a>
                        &nbsp;
                        <a href="${env.BUILD_URL}pipeline/"
                        style="display:inline-block; padding:12px 18px; background:#57606a; color:#ffffff; text-decoration:none; border-radius:5px; font-weight:bold;">
                        View Pipeline Stages
                        </a>
                    </div>

                    <p style="font-size:12px; color:#57606a;">
                        Build URL: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a>
                    </p>

                    <hr style="border:none; border-top:1px solid #d8dee4; margin-top:28px;"/>
                    <p style="font-size:12px; color:#57606a;">
                        Automated notification from Jenkins CI/CD.<br/>
                        Please do not reply to this email.
                    </p>

                </body>
                </html>
                """
            )
        }
    }
}