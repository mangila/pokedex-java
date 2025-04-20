import sys

from shared import run_command


def minikube_destroy(cwd):
    """Delete the k8s resources"""
    destroy = run_command("minikube kubectl -- delete -f k8s.yml", cwd)
    if destroy.stderr is not None:
        print(f"{destroy.stderr}")
        return False
    return True


def main(args):
    """Main execution function."""
    cwd = args[0]
    if not cwd:
        print("No working directory specified")
        sys.exit(1)
    if not minikube_destroy(cwd):
        print("Failed to destroy deployment")
        sys.exit(1)
    print("Deployment destroyed")


if __name__ == "__main__":
    main(sys.argv[:1])
