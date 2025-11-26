#!/bin/bash

# Microservices Docker Management Script
# Usage: ./docker-manage.sh [command]

set -e

COMPOSE_FILE="docker-compose.yml"
PROJECT_NAME="microservices-clinic"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${BLUE}===================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}===================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

build_images() {
    print_header "Building Docker Images"
    docker-compose -p $PROJECT_NAME build
    print_success "Images built successfully"
}

start_services() {
    print_header "Starting Services"
    docker-compose -p $PROJECT_NAME up -d
    
    print_info "Waiting for services to be healthy..."
    sleep 5
    
    # Check health
    check_health
}

stop_services() {
    print_header "Stopping Services"
    docker-compose -p $PROJECT_NAME down
    print_success "Services stopped"
}

restart_services() {
    print_header "Restarting Services"
    docker-compose -p $PROJECT_NAME restart
    print_success "Services restarted"
}

status() {
    print_header "Service Status"
    docker-compose -p $PROJECT_NAME ps
}

logs_all() {
    print_header "Tailing All Logs"
    docker-compose -p $PROJECT_NAME logs -f
}

logs_service() {
    if [ -z "$1" ]; then
        print_error "Please specify a service name"
        echo "Available services: eureka-server, api-gateway, user-service, user-service-db, keycloak, keycloak-db"
        return 1
    fi
    print_header "Logs for $1"
    docker-compose -p $PROJECT_NAME logs -f $1
}

check_health() {
    print_header "Checking Service Health"
    
    services=("eureka-server" "api-gateway" "user-service" "keycloak")
    
    for service in "${services[@]}"; do
        if docker-compose -p $PROJECT_NAME ps $service | grep -q "running"; then
            print_success "$service is running"
        else
            print_error "$service is not running"
        fi
    done
    
    # Check HTTP endpoints
    print_info "Checking HTTP endpoints..."
    
    endpoints=(
        "http://localhost:8761/eureka/apps"
        "http://localhost:8080/actuator/health"
        "http://localhost:8081/actuator/health"
        "http://localhost:8082/health/ready"
    )
    
    names=("Eureka" "Gateway" "User Service" "Keycloak")
    
    for i in "${!endpoints[@]}"; do
        if curl -s "${endpoints[$i]}" > /dev/null 2>&1; then
            print_success "${names[$i]} is responding"
        else
            print_warning "${names[$i]} is not responding yet"
        fi
    done
}

clean() {
    print_header "Cleaning Up"
    read -p "Are you sure you want to remove all containers and volumes? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose -p $PROJECT_NAME down -v
        print_success "Cleanup complete"
    else
        print_info "Cleanup cancelled"
    fi
}

build_and_start() {
    print_header "Full Build and Start"
    build_images
    start_services
    check_health
}

test_endpoints() {
    print_header "Testing Endpoints"
    
    print_info "Testing Eureka..."
    curl -s http://localhost:8761/eureka/apps | jq '.' || print_error "Eureka failed"
    
    print_info "Testing User Service..."
    curl -s http://localhost:8081/actuator/health | jq '.' || print_error "User Service failed"
    
    print_info "Testing Gateway..."
    curl -s http://localhost:8080/actuator/health | jq '.' || print_error "Gateway failed"
    
    print_info "Testing Keycloak..."
    curl -s http://localhost:8082/health/ready | jq '.' || print_error "Keycloak failed"
}

database_shell() {
    print_header "Opening User Service Database Shell"
    docker exec -it user-service-db psql -U clinic_user -d clinic
}

print_usage() {
    echo "Microservices Docker Management Script"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  build              Build Docker images"
    echo "  start              Start all services"
    echo "  stop               Stop all services"
    echo "  restart            Restart all services"
    echo "  status             Show service status"
    echo "  logs               Show logs from all services"
    echo "  logs <service>     Show logs from specific service"
    echo "  health             Check service health"
    echo "  clean              Remove all containers and volumes"
    echo "  build-start        Build and start all services"
    echo "  test               Test all endpoints"
    echo "  db                 Open database shell"
    echo "  help               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 build-start     # Build and start everything"
    echo "  $0 logs user-service"
    echo "  $0 health"
    echo ""
}

# Main
case "${1:-}" in
    build)
        build_images
        ;;
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    status)
        status
        ;;
    logs)
        logs_service "$2"
        ;;
    health)
        check_health
        ;;
    clean)
        clean
        ;;
    build-start)
        build_and_start
        ;;
    test)
        test_endpoints
        ;;
    db)
        database_shell
        ;;
    help|--help|-h)
        print_usage
        ;;
    *)
        print_error "Unknown command: ${1:-}"
        echo ""
        print_usage
        exit 1
        ;;
esac
