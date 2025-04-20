import subprocess
from subprocess import CalledProcessError


class CommandResult:
    def __init__(self, stdout, stderr):
        self.stdout = stdout
        self.stderr = stderr


def run_command(command, cwd) -> CommandResult:
    """Execute a shell command and return its output."""
    try:
        print(command)
        process = subprocess.run(command,
                                 shell=True,
                                 check=True,
                                 cwd=cwd,
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
