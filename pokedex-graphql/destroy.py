import sys

from shared import run_command


def minikube_destroy() -> bool:
    """Delete the k8s resources"""
    destroy = run_command(f"minikube kubectl -- delete -f k8s.yml")
    if destroy.stderr is not None:
        print(f"{destroy.stderr}")
        return False
    return True


def main():
    """Main execution function."""
    if not minikube_destroy():
        print("Failed to destroy deployment")
        sys.exit(1)
    print("Deployment destroyed")


if __name__ == "__main__":
    main()
