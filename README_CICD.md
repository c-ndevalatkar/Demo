# demo1 - Spring Boot Application with GitHub Actions CI/CD

This is a Spring Boot application with a fully automated GitHub Actions CI/CD pipeline.

## 📋 Overview

The project includes a simple REST controller that demonstrates a working Spring Boot application with professional CI/CD practices.

### Application Endpoint

- **GET `/`** - Returns a welcome message: "Hello Nagesh! Welcome to Spring Boot."

## 🚀 CI/CD Pipeline

The GitHub Actions pipeline automates the entire build, test, and deployment process.

### Workflow Files

1. **`.github/workflows/ci-cd.yml`** - Main CI/CD pipeline
   - Runs on push to: `master`, `development`, `QA/*`, `Release/*` branches
   - Runs on pull requests to target branches
   - Stages: Build → Test → Code Quality → Security Scan → Package → Deploy

2. **`.github/workflows/build-test.yml`** - Quick build & test for feature branches
   - Runs on feature branches and all pull requests
   - Fast feedback for developers

### Pipeline Stages

| Stage | Trigger | Purpose |
|-------|---------|---------|
| **Build** | All branches | Compile the Maven project |
| **Test** | All branches | Run unit tests and generate reports |
| **Code Quality** | Target branches only | SonarQube static code analysis |
| **Security Scan** | Target branches only | Vulnerability scanning with Trivy |
| **Secret Detection** | Target branches only | Detect leaked secrets with TruffleHog |
| **Package** | Target branches only | Build deployable JAR/WAR package |
| **Deploy Dev** | development branch | Deploy to development environment |
| **Deploy Stage** | QA/* branches | Deploy to staging environment |
| **Deploy Prod** | Release/* branches | Deploy to production environment |

## 🔧 Setup Instructions

### Prerequisites

- GitHub repository (already set up ✓)
- Git installed
- Maven 3.9.6+
- Java 21

### Step 1: Clone the Repository

```bash
git clone https://github.com/c-ndevalatkar/Demo.git
cd demo1
```

### Step 2: Add GitHub Secrets

The CI/CD pipeline requires secrets for SonarQube and AWS deployments.

**Option A: Manual Setup (GitHub Web UI)**

1. Go to: `Settings` → `Secrets and variables` → `Actions`
2. Click `New repository secret` for each:

**Required Secrets:**

```
SONAR_TOKEN               → Your SonarQube authentication token
AWS_ACCESS_KEY_ID        → AWS access key (for deployments)
AWS_SECRET_ACCESS_KEY    → AWS secret key (for deployments)
AWS_REGION               → AWS region (e.g., us-east-1)
STAGE_S3_BUCKET          → S3 bucket for staging
PROD_S3_BUCKET           → S3 bucket for production
```

**Option B: Automated Setup (PowerShell)**

```powershell
.\setup-github-actions.ps1
```

This script will guide you through adding all secrets interactively.

### Step 3: Verify Setup

1. Go to your GitHub repository
2. Click the **Actions** tab
3. Wait for the workflow to run automatically
4. Check the build status

## 📦 Building Locally

### Build the Project

```bash
mvn clean compile
```

### Run Tests

```bash
mvn test
```

### Package the Application

```bash
mvn package
```

### Run the Application

```bash
mvn spring-boot:run
```

Or if using JAR:
```bash
java -jar target/demo1-1.0.0.jar
```

The application will start on `http://localhost:8080`

Test the endpoint:
```bash
curl http://localhost:8080/
```

Expected output:
```
Hello Nagesh! Welcome to Spring Boot.
```

## 🌿 Git Branch Strategy

### Branch Naming Conventions

- **`master`/`main`** - Production releases (build & test only)
- **`development`** - Development environment (triggers dev deployment)
- **`QA/*`** - Staging environment (triggers stage deployment)
- **`Release/*`** - Production releases (triggers prod deployment)
- **`feature/**`** - Feature branches (quick build & test)
- **`bugfix/**`** - Bug fix branches (quick build & test)
- **`hotfix/**`** - Hotfix branches (quick build & test)

### Typical Workflow

1. Create feature branch:
   ```bash
   git checkout -b feature/my-feature
   ```

2. Make changes and commit:
   ```bash
   git add .
   git commit -m "feat: Add new feature"
   git push origin feature/my-feature
   ```

3. Create Pull Request to `development`
   - CI pipeline automatically runs
   - Reviews required before merge

4. Merge to `development`
   - Triggers deployment to dev environment

5. Create PR to `QA/next-release`
   - Triggers deployment to staging

6. After QA approval, create PR to `Release/v1.x.x`
   - Triggers deployment to production

## 🔒 Security Features

### 1. Trivy Vulnerability Scanner
- Scans dependencies for known vulnerabilities
- Reports findings in GitHub Security tab
- Automated on target branches

### 2. TruffleHog Secret Detection
- Detects exposed API keys, tokens, and secrets
- Scans commit history
- Blocks commits with detected secrets

### 3. SonarQube Code Quality
- Static code analysis
- Code coverage measurements
- Quality gate enforcement
- Code smell detection

## 🚀 Deployment

### Prerequisites for Deployment

- AWS account with CodeDeploy configured
- S3 buckets for each environment (dev, stage, prod)
- IAM user with appropriate permissions:
  - `s3:PutObject`
  - `deploy:CreateDeployment`
  - `deploy:GetDeployment`

### Deployment Triggers

| Branch | Environment | Trigger |
|--------|-------------|---------|
| `development` | Dev | Direct push or merge |
| `QA/*` | Staging | Direct push or merge |
| `Release/*` | Production | Direct push or merge |

### Deployment Process

1. Trigger conditions met (branch push or merge)
2. Build & test stages complete successfully
3. Security scans pass
4. Package created (JAR/WAR + scripts)
5. Uploaded to S3
6. AWS CodeDeploy triggered
7. Application deployed to target environment

### Deployment Status

Check deployment status in:
- GitHub Actions → Workflow run → Deployment logs
- AWS CodeDeploy console (for detailed deployment info)

## 📊 Monitoring & Logs

### GitHub Actions Logs

1. Go to **Actions** tab in your repository
2. Click on any workflow run
3. Click on a job to see detailed logs
4. Logs are archived for 90 days

### Test Reports

- Test results available as artifacts after each run
- Download from workflow run summary

### Security Reports

- Available in **Security** tab → **Code scanning**
- Trivy results shown for identified vulnerabilities

## 🔧 Customization

### Changing SonarQube Project

Edit `.github/workflows/ci-cd.yml`:

```yaml
code-quality:
  script:
    - |
      mvn verify sonar:sonar \
        -Dsonar.projectKey=your-project-key \
        -Dsonar.projectName=your-project-name \
        ...
```

### Adding New Deployment Environment

1. Create new job in `.github/workflows/ci-cd.yml`
2. Set appropriate branch condition
3. Configure AWS credentials
4. Test with feature branch

### Modifying Java Version

Edit both workflow files:

```yaml
- uses: actions/setup-java@v4
  with:
    java-version: '17'  # Change version here
```

## 📝 Environment Configuration

### Development Environment

```yaml
Environment: development
Branch: development
Auto-deploy: Yes
Secrets: Uses repository secrets
```

### Staging Environment

```yaml
Environment: staging
Branch: QA/*
Auto-deploy: Yes
Secrets: Uses repository secrets
```

### Production Environment

```yaml
Environment: production
Branch: Release/*
Auto-deploy: Yes
Secrets: Uses repository secrets
Required Reviewers: Can be configured
```

## 🚨 Troubleshooting

### Build Failures

**Problem:** Maven build fails
- Check Java version (requires Java 21)
- Verify dependencies in `pom.xml`
- Review detailed logs in Actions tab

**Solution:**
```bash
mvn clean compile -X  # Run locally with debug output
```

### Test Failures

**Problem:** Unit tests fail in pipeline
- Run locally: `mvn test`
- Check test output in Actions logs
- Verify test environment setup

### Deployment Failures

**Problem:** Deployment to AWS fails
- Verify AWS credentials (check Secrets)
- Check S3 bucket exists and accessible
- Verify IAM permissions
- Check AWS CodeDeploy configuration

### Secret Issues

**Problem:** "SONAR_TOKEN is not set"
- Add `SONAR_TOKEN` to repository secrets
- Use setup script: `.\setup-github-actions.ps1`
- Verify secret is in correct repository

## 📚 Documentation

- Full CI/CD setup guide: `.github/CICD_SETUP.md`
- GitHub Actions docs: https://docs.github.com/en/actions
- Spring Boot docs: https://spring.io/projects/spring-boot
- Maven docs: https://maven.apache.org/

## 📞 Support & Next Steps

1. ✅ Repository cloned locally
2. ✅ GitHub Actions workflows added
3. ⏳ Configure secrets for your environment
4. ⏳ Test the pipeline with a push
5. ⏳ Monitor in Actions tab
6. 🎉 Enjoy automated CI/CD!

## 📄 License

This project is open source and available under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request to `development`

---

**Last Updated:** July 23, 2026  
**CI/CD Status:** ✅ Configured and Active

