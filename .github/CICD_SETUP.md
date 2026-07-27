# GitHub Actions CI/CD Setup Guide

This document explains how to set up and configure the GitHub Actions CI/CD pipeline for the demo1 project.

## Overview

The CI/CD pipeline includes the following stages:

1. **Build**: Compiles the Maven project
2. **Test**: Runs unit tests and publishes results
3. **Code Quality**: Runs SonarQube analysis (on target branches)
4. **Security Scan**: Vulnerability scanning with Trivy
5. **Secret Detection**: Detects secrets and sensitive data
6. **Package**: Packages the application as JAR/WAR
7. **Deploy**: Deploys to Dev, Stage, and Production environments

## Trigger Conditions

The pipeline is triggered on:
- **Push** to branches: `master`, `main`, `development`, `QA/*`, `Release/*`
- **Pull Requests** to any of the above branches

## Required GitHub Secrets

To enable full CI/CD functionality, add the following secrets in your GitHub repository settings:

### 1. SonarQube Configuration
- **SONAR_TOKEN**: Your SonarQube authentication token
  - Get this from your SonarQube instance: https://your-sonar-instance/account/security

### 2. AWS Configuration (for deployments)
- **AWS_ACCESS_KEY_ID**: Your AWS access key
- **AWS_SECRET_ACCESS_KEY**: Your AWS secret access key
- **AWS_REGION**: AWS region (default: us-east-1)
- **STAGE_S3_BUCKET**: S3 bucket name for staging deployments
- **PROD_S3_BUCKET**: S3 bucket name for production deployments

### How to Add Secrets to GitHub

1. Go to your repository on GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each secret with its name and value

## Workflow Files

- `.github/workflows/ci-cd.yml` - Main CI/CD pipeline

## Branch Strategy

The pipeline respects the following branch patterns:

| Branch Pattern | Actions |
|---|---|
| `master`, `main` | Build, Test (no deployment) |
| `development` | Build, Test, Security Scan, Package, Deploy to Dev |
| `QA/*` | Build, Test, Security Scan, Package, Deploy to Stage |
| `Release/*` | Build, Test, Security Scan, Package, Deploy to Prod |

## Environment Protection

Deployments to `staging` and `production` are protected environments. You may want to:

1. Go to **Settings** → **Environments**
2. Set up required reviewers for each environment
3. Restrict which branches can deploy to each environment

## Customization

### Modifying SonarQube Project

Edit `.github/workflows/ci-cd.yml` and update these values in the `code-quality` job:

```yaml
-Dsonar.projectKey=demo1_project \
-Dsonar.projectName=demo1 \
```

### Adding AWS CodeDeploy Integration

Uncomment and update the AWS CodeDeploy sections in the deployment jobs:

```yaml
# Example:
DEPLOYMENT_ID=$(aws deploy create-deployment \
  --application-name YourAppName \
  --deployment-group-name YourDeploymentGroup \
  --s3-location bucket=your-bucket,key=${S3_KEY},bundleType=zip \
  --region us-east-1 \
  --output text \
  --query "deploymentId")
```

### Changing Java Version

If you need to use a different Java version, update in `.github/workflows/ci-cd.yml`:

```yaml
java-version: '17'  # or your preferred version
```

## Monitoring Builds

1. Go to your GitHub repository
2. Click **Actions** tab
3. View real-time progress of running workflows
4. Click on any workflow run to see detailed logs

## Security Scanning Details

### Trivy Scanner
- Scans for vulnerabilities in dependencies
- Results uploaded to GitHub Security tab
- Run on PRs to target branches

### TruffleHog Secret Scanning
- Detects leaked secrets and sensitive data
- Scans repository history
- Fails on secrets detected (can be overridden with continue-on-error)

### SonarQube Code Quality
- Static code analysis
- Code coverage reports
- Quality gate checks

## Troubleshooting

### Build Fails

1. Check Maven configuration in `pom.xml`
2. Verify Java version compatibility (currently Java 21)
3. Check GitHub Actions logs for error details

### Deployment Fails

1. Verify AWS credentials are correctly set as secrets
2. Check AWS S3 bucket names and permissions
3. Ensure AWS IAM user has required permissions:
   - `s3:PutObject`
   - `s3:GetObject`
   - `deploy:CreateDeployment` (for CodeDeploy)

### Tests Not Running

1. Ensure test files are named `*Test.java` or `*Tests.java`
2. Verify Maven Surefire plugin configuration in `pom.xml`
3. Check test dependencies in `pom.xml`

## Next Steps

1. Add the required secrets to GitHub
2. Push your code to a target branch
3. Monitor the workflow in the **Actions** tab
4. Fix any failures and re-push
5. Once passing, your code is ready for review/deployment

## Support

For more information:
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Maven Documentation](https://maven.apache.org/docs/)
- [SonarQube Documentation](https://docs.sonarqube.org/)

