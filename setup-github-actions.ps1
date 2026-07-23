#!/usr/bin/env pwsh
<#
.SYNOPSIS
GitHub Actions CI/CD Setup Helper Script for demo1 project

.DESCRIPTION
This script helps you configure GitHub Actions secrets and environments
for the demo1 CI/CD pipeline.

.USAGE
.\setup-github-actions.ps1

.AUTHOR
GitHub Actions Setup Script
#>

function Write-Header {
    param([string]$Text)
    Write-Host "`n" -NoNewline
    Write-Host "=" * 60 -ForegroundColor Cyan
    Write-Host $Text -ForegroundColor Cyan
    Write-Host "=" * 60 -ForegroundColor Cyan
}

function Write-Info {
    param([string]$Text)
    Write-Host "ℹ️  $Text" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Text)
    Write-Host "✅ $Text" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Text)
    Write-Host "⚠️  $Text" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Text)
    Write-Host "❌ $Text" -ForegroundColor Red
}

# Main script
Clear-Host
Write-Header "GitHub Actions CI/CD Setup for demo1"

Write-Info "This script will guide you through setting up GitHub Actions secrets"
Write-Info "for your demo1 project."

Write-Host "`nTo use this script, you need to:"
Write-Host "1. Have admin access to your GitHub repository"
Write-Host "2. Have necessary credentials (AWS keys, SonarQube token, etc.)"
Write-Host "3. Have GitHub CLI installed (optional, for automation)"

$response = Read-Host "`nDo you want to continue? (yes/no)"
if ($response -ne "yes") {
    Write-Warning "Setup cancelled."
    exit 0
}

Write-Header "Step 1: Required Secrets"

Write-Info "You need to add the following secrets to GitHub:"
Write-Host ""
Write-Host "SONAR CREDENTIALS:"
Write-Host "  • SONAR_TOKEN - Your SonarQube authentication token"
Write-Host ""
Write-Host "AWS CREDENTIALS (for deployments):"
Write-Host "  • AWS_ACCESS_KEY_ID - Your AWS access key"
Write-Host "  • AWS_SECRET_ACCESS_KEY - Your AWS secret access key"
Write-Host "  • AWS_REGION - AWS region (e.g., us-east-1)"
Write-Host "  • STAGE_S3_BUCKET - S3 bucket for staging"
Write-Host "  • PROD_S3_BUCKET - S3 bucket for production"

Write-Header "Step 2: GitHub Web UI (Manual Setup)"

Write-Info "Follow these steps to add secrets manually:"
Write-Host ""
Write-Host "1. Go to your GitHub repository"
Write-Host "2. Navigate to Settings → Secrets and variables → Actions"
Write-Host "3. Click 'New repository secret' for each secret:"
Write-Host ""

$secrets = @{
    "SONAR_TOKEN" = "Your SonarQube token"
    "AWS_ACCESS_KEY_ID" = "Your AWS access key ID"
    "AWS_SECRET_ACCESS_KEY" = "Your AWS secret access key"
    "AWS_REGION" = "AWS region (default: us-east-1)"
    "STAGE_S3_BUCKET" = "S3 bucket for staging"
    "PROD_S3_BUCKET" = "S3 bucket for production"
}

$secretNumber = 1
foreach ($secret in $secrets.GetEnumerator()) {
    Write-Host "   Secret $secretNumber - $($secret.Key)"
    Write-Host "      Description: $($secret.Value)"
    Write-Host ""
    $secretNumber++
}

Write-Header "Step 3: GitHub CLI Setup (Optional, Automated)"

$useGHCLI = Read-Host "Do you have GitHub CLI installed and want to add secrets automatically? (yes/no)"

if ($useGHCLI -eq "yes") {
    # Check if GitHub CLI is installed
    $ghExists = $null -ne (Get-Command gh -ErrorAction SilentlyContinue)

    if (-not $ghExists) {
        Write-Error "GitHub CLI is not installed or not in PATH."
        Write-Info "Install it from: https://cli.github.com/"
    }
    else {
        Write-Success "GitHub CLI found!"

        # Check if user is authenticated
        Write-Info "Checking GitHub authentication..."
        $ghStatus = gh auth status 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Success "GitHub CLI is authenticated"

            # Get repository information
            $repoOwner = gh repo view --json owner --jq ".owner.login"
            $repoName = gh repo view --json nameWithOwner --jq ".nameWithOwner"

            Write-Info "Repository: $repoName"

            Write-Host "`nEnter your secret values (press Enter to skip):`n"

            $sonarToken = Read-Host "SONAR_TOKEN"
            if ($sonarToken) {
                gh secret set SONAR_TOKEN -b $sonarToken
                Write-Success "SONAR_TOKEN set"
            }

            $awsAccessKey = Read-Host "AWS_ACCESS_KEY_ID"
            if ($awsAccessKey) {
                gh secret set AWS_ACCESS_KEY_ID -b $awsAccessKey
                Write-Success "AWS_ACCESS_KEY_ID set"
            }

            $awsSecretKey = Read-Host "AWS_SECRET_ACCESS_KEY"
            if ($awsSecretKey) {
                gh secret set AWS_SECRET_ACCESS_KEY -b $awsSecretKey
                Write-Success "AWS_SECRET_ACCESS_KEY set"
            }

            $awsRegion = Read-Host "AWS_REGION (default: us-east-1)"
            if ($awsRegion) {
                gh secret set AWS_REGION -b $awsRegion
                Write-Success "AWS_REGION set"
            }

            $stageBucket = Read-Host "STAGE_S3_BUCKET"
            if ($stageBucket) {
                gh secret set STAGE_S3_BUCKET -b $stageBucket
                Write-Success "STAGE_S3_BUCKET set"
            }

            $prodBucket = Read-Host "PROD_S3_BUCKET"
            if ($prodBucket) {
                gh secret set PROD_S3_BUCKET -b $prodBucket
                Write-Success "PROD_S3_BUCKET set"
            }

            Write-Host ""
            Write-Success "All secrets have been configured!"
        }
        else {
            Write-Warning "GitHub CLI is not authenticated. Run 'gh auth login' first."
        }
    }
}

Write-Header "Step 4: Configure Environments (Optional)"

Write-Info "GitHub Environments allow you to set up deployment rules."
Write-Info "You can configure required reviewers and branch restrictions."

$configEnv = Read-Host "`nDo you want to set up environment-specific rules? (yes/no)"

if ($configEnv -eq "yes") {
    Write-Host "`nTo configure environments:"
    Write-Host "1. Go to your GitHub repository"
    Write-Host "2. Navigate to Settings → Environments"
    Write-Host "3. Create/edit these environments:"
    Write-Host "   • development"
    Write-Host "   • staging"
    Write-Host "   • production"
    Write-Host ""
    Write-Host "For each environment, you can:"
    Write-Host "   • Set required reviewers"
    Write-Host "   • Restrict deployments to specific branches"
    Write-Host "   • Add environment secrets (overrides repo secrets)"
}

Write-Header "Step 5: Verify Setup"

Write-Info "To verify your setup:"
Write-Host ""
Write-Host "1. Push code to a target branch (development, QA/*, Release/*)"
Write-Host "2. Go to the 'Actions' tab in your GitHub repository"
Write-Host "3. Watch the workflow run"
Write-Host "4. Check the logs for any failures"

Write-Header "Step 6: Next Steps"

Write-Host ""
Write-Host "✨ Your GitHub Actions CI/CD pipeline is now ready!"
Write-Host ""
Write-Host "Next actions:"
Write-Host "1. Commit and push the .github/workflows files to your repository"
Write-Host "2. Add the required secrets to GitHub"
Write-Host "3. Test the pipeline by pushing code to a target branch"
Write-Host "4. Monitor the Actions tab for pipeline runs"
Write-Host ""
Write-Host "For more information, see: .github/CICD_SETUP.md"
Write-Host ""

Write-Success "Setup helper script completed!"

