import subprocess
import sys
from subprocess import CalledProcessError

SCHEDULER_CWD = "./pokedex-scheduler"
GRAPHQL_CWD = "./pokedex-graphql"


class CommandResult:
    def __init__(self, stdout, stderr):
        self.stdout = stdout
        self.stderr = stderr


def run_python_script(command, cwd) -> CommandResult:
    """Execute a shell python command and return its output."""
    try:
        process = subprocess.run(f"{sys.executable} {command}",
                                 shell=True,
                                 cwd=cwd,
                                 check=True,
                                 text=True,
                                 stdout=subprocess.PIPE,
                                 stderr=subprocess.PIPE)
        return CommandResult(process.stdout, None)
    except CalledProcessError as e:
        return CommandResult(None, e.stderr)
    except Exception as e:
        print(f"Unexpected error: {str(e)}")
        return CommandResult(None, str(e))


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


def apply_external_database() -> bool:
    """Deploy the Docker image to Minikube and apply the k8s spec"""
    destroy = run_command("minikube kubectl -- delete -f k8s-external-database.yml")
    if destroy.stderr is not None:
        print(f"{destroy.stderr}")

    apply = run_command("minikube kubectl -- apply -f k8s-external-database.yml")
    if apply.stderr is not None:
        print(f"{apply.stderr}")
        return False

    return True


if __name__ == "__main__":
    if not apply_external_database():
        print("Failed to apply external database")
        sys.exit(1)
    destroy_scheduler = run_python_script("destroy.py", SCHEDULER_CWD)
    if destroy_scheduler.stderr is not None:
        print(f"{destroy_scheduler.stderr}")
    destroy_graphql = run_python_script("destroy.py", GRAPHQL_CWD)
    if destroy_scheduler.stderr is not None:
        print(f"{destroy_graphql.stderr}")
    apply_scheduler = run_python_script("apply.py", SCHEDULER_CWD)
    if apply_scheduler.stderr is not None:
        print(f"{apply_scheduler.stderr}")
        sys.exit(1)
    apply_graphql = run_python_script("apply.py", GRAPHQL_CWD)
    if apply_graphql.stderr is not None:
        print(f"{apply_graphql.stderr}")
        sys.exit(1)
