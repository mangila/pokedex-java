import platform
import sys

from shared import run_command, DOCKER_TAG


def build_jar_file() -> bool:
    """Build the Java project"""
    os = platform.system().lower()
    if os == "windows":
        build = run_command("mvnw.cmd clean package")
        if build.stderr is not None:
            print(f"{build.stderr}")
            return False
    elif os == "linux":
        build = run_command("mvnw.sh clean package")
        if build.stderr is not None:
            print(f"{build.stderr}")
            return False
    else:
        print(f"Unsupported OS: {os}")
        return False
    return True


def build_docker_image() -> bool:
    """Build a Docker image and create a tarball."""
    build = run_command(f"docker build --tag {DOCKER_TAG} .")
    if build.stderr is not None:
        print(f"{build.stderr}")
        return False
    save = run_command(f"docker save -o {DOCKER_TAG}.tar {DOCKER_TAG}")
    if save.stderr is not None:
        print(f"{save.stderr}")
        return False

    return True


def main():
    """Main execution function."""
    if not build_jar_file():
        print("Failed to build Java project")
        sys.exit(1)
    if not build_docker_image():
        print("Failed to build Docker image")
        sys.exit(1)
    print("Build successful")

if __name__ == "__main__":
    main()
