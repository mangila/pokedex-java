package com.github.mangila.pokedex.database.model;

import com.github.mangila.pokedex.shared.util.Ensure;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WalFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFile.class);
    private static final Set<StandardOpenOption> OPEN_OPTIONS = Set.of(
            StandardOpenOption.WRITE,
            StandardOpenOption.READ,
            StandardOpenOption.SYNC);
    private static final byte[] MAGIC_NUMBER = "WAL".getBytes(Charset.defaultCharset());
    private final Path path;
    private final ExecutorService walChannelExecutor;
    private WalFileChannel channel;
    private AtomicReference<WalFileStatus> status = new AtomicReference<>(WalFileStatus.CLOSED);

    public WalFile(Path path) {
        Ensure.notNull(path, "Path must not be null");
        this.path = path;
        this.walChannelExecutor = VirtualThreadFactory.newFixedThreadPool(10);
    }

    public void open(WalTable walTable) throws IOException {
        LOGGER.info("Opening WAL file {}", path);
        if (!Files.exists(path)) {
            Files.createFile(path);
            Files.write(path, MAGIC_NUMBER);
            LOGGER.info("Created new WAL file {}", path);
        }
        channel = new WalFileChannel(AsynchronousFileChannel.open(path,
                OPEN_OPTIONS,
                walChannelExecutor)
        );
        status.set(WalFileStatus.OPEN);
        load(walTable);
    }

    private void load(WalTable walTable) throws IOException {
        Buffer readBuffer = Buffer.from((int) channel.size());
        CompletableFuture<WalAppendStatus> readFuture = new CompletableFuture<>();
        channel.read(new WalFileChannel.Attachment(readFuture, 0, readBuffer.remaining(), readBuffer));
        readFuture.join();
        readBuffer.flip();
        byte[] magicNumber = readBuffer.getArray(MAGIC_NUMBER.length);
        Ensure.equals(magicNumber, MAGIC_NUMBER, "WAL file magic number not equals");
        if (!readBuffer.isEmpty()) {
            // TODO: load WalTable
            walTable.put(new Key("ASDF"), new Field("ASDF"), new Value(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0}));
            LOGGER.debug("Loaded {} entries to {}", 1, path);
        } else {
            LOGGER.debug("File {} is empty", path);
        }
        LOGGER.info("Loaded WAL file {}", path);
    }

    public void close() throws IOException {
        LOGGER.info("Closing WAL file {}", path);
        channel.close();
        VirtualThreadFactory.terminateGracefully(walChannelExecutor, Duration.ofSeconds(30));
        status.set(WalFileStatus.CLOSED);
    }

    public void delete() throws IOException {
        LOGGER.info("Deleting WAL file {}", path);
        Files.deleteIfExists(path);
    }

    public WalFileChannel channel() {
        return channel;
    }

    public AtomicReference<WalFileStatus> status() {
        return status;
    }

    public Path getPath() {
        return path;
    }

    public long getRotation() {
        String name = path.getFileName().toString();
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(name.substring(name.lastIndexOf("-") + 1));
        if (m.find()) {
            return Integer.parseInt(m.group());
        }
        throw new IllegalStateException("No rotation number found in file name: " + name);
    }
}
