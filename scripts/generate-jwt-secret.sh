#!/bin/bash
# Generate a cryptographically secure JWT secret for Lambda deployment

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}JWT Secret Generator${NC}"
echo "=========================================="
echo ""

# Check if OpenSSL is available
if ! command -v openssl &> /dev/null; then
    echo -e "${RED}Error: OpenSSL is not installed${NC}"
    echo "Please install OpenSSL first:"
    echo "  macOS: brew install openssl"
    echo "  Ubuntu: sudo apt-get install openssl"
    echo "  CentOS: sudo yum install openssl"
    exit 1
fi

# Generate a 64-character random secret (48 bytes of entropy encoded as base64)
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')

echo -e "${GREEN}Generated JWT Secret (64 characters):${NC}"
echo ""
echo "$JWT_SECRET"
echo ""
echo "=========================================="
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo "1. Copy the secret above"
echo "2. Update terraform/terraform.tfvars:"
echo "   jwt_secret = \"$JWT_SECRET\""
echo ""
echo "3. Keep this secret secure - never commit to git"
echo "4. Store in AWS Secrets Manager for production"
echo ""
