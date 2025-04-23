import platform
import sys

from run_cmd import run_cmd

CURRENT_OS = platform.system().lower()


def run_mvn_command(command: str, working_dir: str = "."):
    """ Run a Maven command. """
    if CURRENT_OS == "windows":
        run_cmd(f"mvnw.cmd {command}", cwd=working_dir)
    else:
        run_cmd(f"./mvn.sh {command}", cwd=working_dir)


if __name__ == "__main__":
    mvn_command = sys.argv[1]
    cwd = sys.argv[2]
    run_mvn_command(command=mvn_command, working_dir=cwd)
