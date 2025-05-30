package com.github.mangila.pokedex.shared.database.internal.file;

import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;

public final class FileOptions {

    public static final Set<StandardOpenOption> CREATE_NEW_WRITE_OPTIONS = EnumSet.of(
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE_NEW,
            StandardOpenOption.SPARSE,
            StandardOpenOption.DSYNC);

    public static final Set<StandardOpenOption> WRITE_OPTIONS = EnumSet.of(
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.SPARSE,
            StandardOpenOption.DSYNC);

    public static final Set<StandardOpenOption> READ_OPTIONS = EnumSet.of(
            StandardOpenOption.READ,
            StandardOpenOption.DSYNC);
}
