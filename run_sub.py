import os
import subprocess
import sys


def run_subprocess(command, work_dir):
    """
    Run a subprocess with the given command in the specified working directory.
    
    Args:
        command (str): The command to execute
        work_dir (str): The working directory where the command should be executed
    """
    try:
        # Check if the working directory exists
        if not os.path.isdir(work_dir):
            print(f"Error: Working directory '{work_dir}' does not exist.")
            sys.exit(1)

        # Run the subprocess
        process = subprocess.Popen(
            command,
            shell=True,
            cwd=work_dir,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            universal_newlines=True
        )

        # Get the output and error
        stdout, stderr = process.communicate()

        # Print the output
        if stdout:
            print(stdout)

        # Print the error if any
        if stderr:
            print(f"Error: {stderr}")

        # Return the exit code
        return process.returncode
    except Exception as e:
        print(f"Error executing command: {e}")
        return 1


if __name__ == "__main__":
    # Check if the required arguments are provided
    if len(sys.argv) < 3:
        print("Usage: python run_sub.py COMMAND WORK_DIR")
        sys.exit(1)

    # Get the command and working directory from command-line arguments
    command = sys.argv[1]
    work_dir = sys.argv[2]

    # Run the subprocess
    exit_code = run_subprocess(command, work_dir)

    # Exit with the same exit code as the subprocess
    sys.exit(exit_code)
