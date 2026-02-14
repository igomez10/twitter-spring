#!/bin/bash
# Unified deployment script for AWS Lambda deployment

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
TERRAFORM_DIR="${PROJECT_ROOT}/terraform"

# Functions
print_usage() {
    cat << EOF
${BLUE}Twitter Spring - AWS Lambda Deployment${NC}

Usage: $0 <command> [options]

Commands:
  init       Initialize Terraform
  plan       Preview infrastructure changes
  apply      Create/update infrastructure (requires manual image URI)
  build      Build and push Docker image to ECR
  deploy     Full deployment (Terraform + image update)
  destroy    Clean up all AWS resources
  status     Show deployment status
  help       Show this help message

Examples:
  # Initial setup
  $0 init
  $0 plan

  # First deployment
  $0 build
  $0 apply

  # Full redeploy
  $0 deploy

  # Cleanup
  $0 destroy

${YELLOW}Prerequisites:${NC}
  - AWS CLI configured with credentials
  - Terraform >= 1.6
  - Docker with buildx support
  - terraform/terraform.tfvars created

EOF
}

print_header() {
    echo ""
    echo -e "${BLUE}=========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}=========================================${NC}"
    echo ""
}

check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"

    # Check Terraform
    if ! command -v terraform &> /dev/null; then
        echo -e "${RED}Error: Terraform not found. Please install Terraform >= 1.6${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Terraform $(terraform version -json | grep terraform_version | cut -d'"' -f4)${NC}"

    # Check AWS CLI
    if ! command -v aws &> /dev/null; then
        echo -e "${RED}Error: AWS CLI not found. Please install AWS CLI${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ AWS CLI${NC}"

    # Check Docker
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}Error: Docker not found. Please install Docker${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Docker${NC}"

    # Check terraform.tfvars
    if [ ! -f "${TERRAFORM_DIR}/terraform.tfvars" ]; then
        echo -e "${RED}Error: ${TERRAFORM_DIR}/terraform.tfvars not found${NC}"
        echo "Please create it from terraform.tfvars.example"
        exit 1
    fi
    echo -e "${GREEN}✓ terraform.tfvars found${NC}"

    echo ""
}

cmd_init() {
    print_header "Initializing Terraform"

    check_prerequisites

    cd "$TERRAFORM_DIR"
    terraform init

    echo -e "${GREEN}✓ Terraform initialized successfully${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Review and execute: $0 plan"
    echo "2. Apply changes: $0 apply"
}

cmd_plan() {
    print_header "Planning Infrastructure Changes"

    check_prerequisites

    cd "$TERRAFORM_DIR"
    terraform plan -out=tfplan

    echo ""
    echo -e "${YELLOW}Review the plan above carefully${NC}"
    echo "Apply with: $0 apply"
}

cmd_apply() {
    print_header "Applying Infrastructure"

    check_prerequisites

    cd "$TERRAFORM_DIR"

    # Check if tfplan exists
    if [ -f tfplan ]; then
        terraform apply tfplan
        rm -f tfplan
    else
        terraform apply
    fi

    # Output important values
    echo ""
    echo -e "${GREEN}✓ Infrastructure deployed successfully${NC}"
    echo ""
    echo "Important outputs:"
    terraform output -json | grep -E '(api_endpoint|ecr_repository|lambda_function)' || true
}

cmd_build() {
    print_header "Building and Pushing Docker Image"

    IMAGE_TAG="${1:-latest}"

    check_prerequisites

    # Run build script
    bash "${SCRIPT_DIR}/build-and-push.sh" "$IMAGE_TAG"
}

cmd_deploy() {
    print_header "Full Deployment"

    check_prerequisites

    # Step 1: Initialize if needed
    if [ ! -d "${TERRAFORM_DIR}/.terraform" ]; then
        echo "Initializing Terraform..."
        cmd_init
    fi

    # Step 2: Plan changes
    echo "Planning infrastructure changes..."
    cmd_plan

    # Step 3: Build and push image
    echo "Building Docker image..."
    cmd_build "latest"

    # Get ECR URI for Lambda update
    AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    AWS_REGION=$(grep 'aws_region' "${TERRAFORM_DIR}/terraform.tfvars" | cut -d'"' -f2)
    APP_NAME=$(grep 'app_name' "${TERRAFORM_DIR}/terraform.tfvars" | cut -d'"' -f2)
    ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${APP_NAME}"

    echo ""
    echo -e "${YELLOW}Updating Lambda function with new image...${NC}"
    echo "Image: ${ECR_URI}:latest"

    # Step 4: Apply infrastructure
    cd "$TERRAFORM_DIR"
    terraform apply \
        -var="lambda_image_uri=${ECR_URI}:latest" \
        -auto-approve

    echo ""
    echo -e "${GREEN}✓ Deployment completed successfully!${NC}"
    echo ""
    echo "API Endpoint:"
    terraform output -raw api_endpoint_url 2>/dev/null || echo "Run: terraform output api_endpoint_url"
}

cmd_destroy() {
    print_header "Destroying AWS Resources"

    echo -e "${RED}WARNING: This will delete all AWS resources!${NC}"
    echo "Resources to be deleted:"
    echo "  - Lambda function"
    echo "  - API Gateway"
    echo "  - ECR repository"
    echo "  - Secrets Manager secret"
    echo "  - CloudWatch log groups"
    echo "  - IAM roles and policies"
    echo ""
    read -p "Are you sure? Type 'yes' to confirm: " confirm

    if [ "$confirm" != "yes" ]; then
        echo "Cancelled"
        exit 0
    fi

    check_prerequisites

    cd "$TERRAFORM_DIR"
    terraform destroy -auto-approve

    echo -e "${GREEN}✓ All resources destroyed${NC}"
}

cmd_status() {
    print_header "Deployment Status"

    check_prerequisites

    if [ ! -d "${TERRAFORM_DIR}/.terraform" ]; then
        echo "Terraform not initialized"
        return
    fi

    cd "$TERRAFORM_DIR"

    echo -e "${YELLOW}Terraform State:${NC}"
    terraform show -json | jq '.values.outputs' 2>/dev/null || terraform output

    echo ""
    echo -e "${YELLOW}AWS Lambda Status:${NC}"
    APP_NAME=$(grep 'app_name' terraform.tfvars | cut -d'"' -f2)
    ENVIRONMENT=$(grep 'environment' terraform.tfvars | cut -d'"' -f2)
    FUNCTION_NAME="${APP_NAME}-${ENVIRONMENT}"

    aws lambda get-function --function-name "$FUNCTION_NAME" \
        --query 'Configuration.[FunctionName,State,LastModified,MemorySize,Timeout]' \
        --output table 2>/dev/null || echo "Function not found"
}

# Main script
COMMAND="${1:-help}"

case "$COMMAND" in
    init)
        cmd_init
        ;;
    plan)
        cmd_plan
        ;;
    apply)
        cmd_apply
        ;;
    build)
        cmd_build "${2:-latest}"
        ;;
    deploy)
        cmd_deploy
        ;;
    destroy)
        cmd_destroy
        ;;
    status)
        cmd_status
        ;;
    help|--help|-h)
        print_usage
        ;;
    *)
        echo -e "${RED}Unknown command: $COMMAND${NC}"
        echo ""
        print_usage
        exit 1
        ;;
esac
