import subprocess
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
