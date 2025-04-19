import platform
import subprocess
import sys
from subprocess import CalledProcessError

DOCKER_TAG = "pokedex-scheduler"


class CommandResult:
    def __init__(self, stdout, stderr):
        self.stdout = stdout
        self.stderr = stderr


def run_command(command) -> CommandResult:
    """Execute a shell command and return its output."""
    try:
        print(command)
        process = subprocess.run(command,
                                 shell=True,
                                 check=True,
                                 text=True,
                                 stdout=subprocess.PIPE,
                                 stderr=subprocess.PIPE)
        print(process.stdout)
        return CommandResult(process.stdout, None)
    except CalledProcessError as e:
        return CommandResult(None, e.stderr)
    except Exception as e:
        print(f"Unexpected error: {str(e)}")
        return CommandResult(None, str(e))


def build_jar_file():
    """Build the Java project"""
    os = platform.system().lower()
    if os == "windows":
        build = run_command("mvnw.cmd clean package")
        if build.stderr is not None:
            print(f"{build.stderr}")
            return False
    elif os == "linux":
        build = run_command("mvnw.sh clean package")
        if build.stderr is not None:
            print(f"{build.stderr}")
            return False
    else:
        print(f"Unsupported OS: {os}")
        return False
    return True


def check_minikube_status() -> bool:
    """ Check if Minikube is running """
    is_running = "Running" in run_command("minikube status").stdout
    return is_running


def build_docker_image():
    """Build a Docker image and create a tarball."""
    build = run_command(f"docker build --tag {DOCKER_TAG} .")
    if build.stderr is not None:
        print(f"{build.stderr}")
        return False
    save = run_command(f"docker save -o {DOCKER_TAG}.tar {DOCKER_TAG}")
    if save.stderr is not None:
        print(f"{save.stderr}")
        return False

    return True


def minikube_deploy():
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
    if not build_jar_file():
        print("Failed to build Java project")
        sys.exit(1)
    if not check_minikube_status():
        print("Minikube is not running")
        sys.exit(1)
    if not build_docker_image():
        print("Failed to build Docker image")
        sys.exit(1)
    if not minikube_deploy():
        print("Failed to deploy to Minikube")
        sys.exit(1)
    print("Deployment successful")


if __name__ == "__main__":
    main()
