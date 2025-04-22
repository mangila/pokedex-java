import platform
import sys

from mvn import run_mvn_command
from run_cmd import run_cmd

SERVICES = [
    {
        'cwd': './scheduler',
        "mvn_modules": "scheduler,shared",
        'docker_tag': 'pokedex-scheduler'
    },
    {
        'cwd': './graphql',
        "mvn_modules": "graphql,shared",
        'docker_tag': 'pokedex-graphql'
    }
]


def build_docker_image(working_dir: str, docker_tag: str):
    """Build a Docker image and save to a tarball."""
    run_cmd(f"docker build --tag {docker_tag} .", working_dir)
    tarball_name = f"{docker_tag}.tar"
    run_cmd(f"docker save -o {tarball_name} {docker_tag}", working_dir)


if __name__ == "__main__":
    """Main execution function."""
    run_cmd("minikube status")
    run_cmd("minikube kubectl create namespace pokedex")
    run_cmd("minikube kubectl -- delete -f k8s-external-database.yml")
    run_cmd("minikube kubectl -- apply -f k8s-external-database.yml")

    for service in SERVICES:
        run_mvn_command(command=f"clean package -pl {service['mvn_modules']} -am")
        build_docker_image(working_dir=service['cwd'], docker_tag=service['docker_tag'])
        run_cmd(command="minikube kubectl -- delete -f k8s.yml", cwd=service['cwd'])
        run_cmd(command=f"minikube image load {service['docker_tag']}.tar", cwd=service['cwd'])
        run_cmd(command=f"minikube kubectl -- apply -f k8s.yml", cwd=service['cwd'])
