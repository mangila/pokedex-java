import platform
import sys

from shared import run_command


def build_jar_file(cwd) -> bool:
    """Build the Java project"""
    os = platform.system().lower()
    if os == "windows":
        build = run_command("mvnw.cmd clean package", cwd)
        if build.stderr is not None:
            print(f"{build.stderr}")
            return False
    elif os == "linux":
        build = run_command("mvnw.sh clean package", cwd)
        if build.stderr is not None:
            print(f"{build.stderr}")
            return False
    else:
        print(f"Unsupported OS: {os}")
        return False
    return True


def build_docker_image(cwd, docker_tag) -> bool:
    """Build a Docker image and create a tarball."""
    build = run_command(f"docker build --tag {docker_tag} .", cwd)
    if build.stderr is not None:
        print(f"{build.stderr}")
        return False
    save = run_command(f"docker save -o {docker_tag}.tar {docker_tag}", cwd)
    if save.stderr is not None:
        print(f"{save.stderr}")
        return False

    return True


def main(args):
    """Main execution function."""
    if not args:
        print("No args specified")
        sys.exit(1)
    cwd = args[0]
    docker_tag = args[1]
    if not build_jar_file(cwd):
        print("Failed to build Java project")
        sys.exit(1)
    if not build_docker_image(cwd, docker_tag):
        print("Failed to build Docker image")
        sys.exit(1)
    print("Build successful")


if __name__ == "__main__":
    main(sys.argv[1:])
