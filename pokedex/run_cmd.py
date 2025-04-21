import subprocess
from subprocess import CalledProcessError


def run_cmd(command, cwd="."):
    """Execute a shell command and return its output."""
    try:
        print(command)
        process = subprocess.run(command,
                                 shell=True,
                                 check=True,
                                 cwd=cwd,
                                 text=True,
                                 timeout=700,
                                 stdout=subprocess.PIPE,
                                 stderr=subprocess.PIPE)
        print(process.stdout)
    except CalledProcessError as e:
        print(f"Command failed with error: {e.stderr}")
    except Exception as e:
        print(f"Unexpected error: {str(e)}")
