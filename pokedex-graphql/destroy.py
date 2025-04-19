import subprocess
import sys
from subprocess import CalledProcessError


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


def minikube_destroy():
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
