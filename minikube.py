import subprocess
import sys
from shared import CommandResult, run_command
from subprocess import CalledProcessError

SCHEDULER_CWD = "./pokedex-scheduler"
SCHEDULER_DOCKER_TAG = "pokedex-scheduler"
GRAPHQL_CWD = "./pokedex-graphql"
GRAPHQL_DOCKER_TAG = "pokedex-graphql"


def apply_external_name() -> bool:
    """Apply the k8s external name spec"""
    destroy = run_command("minikube kubectl -- delete -f k8s-external-database.yml", ".")
    if destroy.stderr is not None:
        print(f"{destroy.stderr}")

    apply = run_command("minikube kubectl -- apply -f k8s-external-database.yml", ".")
    if apply.stderr is not None:
        print(f"{apply.stderr}")
        return False

    return True


if __name__ == "__main__":
    if not apply_external_name():
        print("Failed to apply external names")
        sys.exit(1)

    destroy_scheduler = run_command(f"python destroy.py {SCHEDULER_CWD} {SCHEDULER_DOCKER_TAG}", ".")
    if destroy_scheduler.stderr is not None:
        print(f"{destroy_scheduler.stderr}")

    destroy_graphql = run_command(f"python destroy.py {GRAPHQL_CWD} {GRAPHQL_DOCKER_TAG}", ".")
    if destroy_graphql.stderr is not None:
        print(f"{destroy_graphql.stderr}")

    build_scheduler = run_command(f"python build.py {SCHEDULER_CWD} {SCHEDULER_DOCKER_TAG}", ".")
    if build_scheduler.stderr is not None:
        print(f"{build_scheduler.stderr}")
        sys.exit(1)

    build_graphql = run_command(f"python build.py {GRAPHQL_CWD} {GRAPHQL_DOCKER_TAG}", ".")
    if build_graphql.stderr is not None:
        print(f"{build_graphql.stderr}")
        sys.exit(1)

    apply_scheduler = run_command(f"python apply.py {SCHEDULER_CWD} {SCHEDULER_DOCKER_TAG}", ".")
    if apply_scheduler.stderr is not None:
        print(f"{apply_scheduler.stderr}")
        sys.exit(1)

    apply_graphql = run_command(f"python apply.py {GRAPHQL_CWD} {GRAPHQL_DOCKER_TAG}", ".")
    if apply_graphql.stderr is not None:
        print(f"{apply_graphql.stderr}")
        sys.exit(1)
