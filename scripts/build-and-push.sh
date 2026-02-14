#!/bin/bash
# Build Docker image for ARM64 (Graviton2) and push to ECR

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DOCKERFILE="${PROJECT_ROOT}/Dockerfile.lambda"

# Load environment from terraform
TERRAFORM_DIR="${PROJECT_ROOT}/terraform"
if [ -f "${TERRAFORM_DIR}/terraform.tfvars" ]; then
    source <(grep -E '^\s*(aws_region|app_name)\s*=' "${TERRAFORM_DIR}/terraform.tfvars" | sed 's/ //g')
else
    echo -e "${RED}Error: terraform/terraform.tfvars not found${NC}"
    echo "Please run the deploy script or create terraform.tfvars first"
    exit 1
fi

# Set defaults if not found
AWS_REGION="${aws_region:-us-west-2}"
APP_NAME="${app_name:-twitter-spring}"
IMAGE_TAG="${1:-latest}"

echo -e "${BLUE}Docker Build & Push for AWS Lambda${NC}"
echo "=========================================="
echo "Region: $AWS_REGION"
echo "App: $APP_NAME"
echo "Tag: $IMAGE_TAG"
echo ""

# Check Docker is running
if ! docker ps &> /dev/null; then
    echo -e "${RED}Error: Docker daemon is not running${NC}"
    echo "Please start Docker and try again"
    exit 1
fi

# Get AWS account ID
echo -e "${YELLOW}Getting AWS account ID...${NC}"
if ! command -v aws &> /dev/null; then
    echo -e "${RED}Error: AWS CLI is not installed${NC}"
    exit 1
fi

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
ECR_REPOSITORY="${APP_NAME}"
ECR_URI="${ECR_REGISTRY}/${ECR_REPOSITORY}"

echo "ECR URI: $ECR_URI"
echo ""

# Build Docker image for ARM64 (Lambda compatible)
echo -e "${YELLOW}Building Docker image for ARM64...${NC}"
docker buildx build \
    --platform linux/arm64 \
    --tag "${ECR_URI}:${IMAGE_TAG}" \
    --tag "${ECR_URI}:latest" \
    --file "$DOCKERFILE" \
    --push \
    "$PROJECT_ROOT" || {
    echo -e "${RED}Docker build failed${NC}"
    echo "Make sure:"
    echo "1. Docker buildx is installed: docker buildx create --use"
    echo "2. You are logged in to ECR"
    exit 1
}

# Authenticate with ECR
echo ""
echo -e "${YELLOW}Authenticating with ECR...${NC}"
aws ecr get-login-password --region "$AWS_REGION" | \
    docker login --username AWS --password-stdin "$ECR_REGISTRY" || {
    echo -e "${RED}ECR authentication failed${NC}"
    exit 1
}

# Push image (already done with --push in buildx, but verify)
echo ""
echo -e "${GREEN}✓ Docker image built and pushed successfully${NC}"
echo ""
echo "Image URI: ${ECR_URI}:${IMAGE_TAG}"
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo "1. Update terraform/lambda.tf with the image URI:"
echo "   lambda_image_uri = \"${ECR_URI}:${IMAGE_TAG}\""
echo ""
echo "2. Deploy Lambda function:"
echo "   cd $TERRAFORM_DIR && terraform apply"
echo ""
