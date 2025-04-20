import sys

from shared import run_command, DOCKER_TAG



def check_minikube_status() -> bool:
    """ Check if Minikube is running """
    return "Running" in run_command("minikube status").stdout


def minikube_deploy() -> bool:
    """Deploy the Docker image to Minikube and apply the k8s spec"""
    load = run_command(f"minikube image load {DOCKER_TAG}.tar")
    if load.stderr is not None:
        print(f"{load.stderr}")
        return False
    apply = run_command(f"minikube kubectl -- apply -f k8s.yml")
    if apply.stderr is not None:
        print(f"{apply.stderr}")
        return False

    return True


def main():
    """Main execution function."""
    if not check_minikube_status():
        print("Minikube is not running")
        sys.exit(1)
    if not minikube_deploy():
        print("Failed to deploy to Minikube")
        sys.exit(1)
    print("Deployment successful")


if __name__ == "__main__":
    main()
