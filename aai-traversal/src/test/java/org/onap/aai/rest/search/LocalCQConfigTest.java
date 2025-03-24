package org.onap.aai.rest.search;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

class LocalCQConfigTest {

    private LocalCQConfig localCQConfig;
    private Path storedQueriesFilePath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException, NoSuchFieldException, IllegalAccessException {
        localCQConfig = new LocalCQConfig();

        Field storedQueriesLocationField = LocalCQConfig.class.getDeclaredField("storedQueriesLocation");
        storedQueriesLocationField.setAccessible(true);
        storedQueriesLocationField.set(localCQConfig, tempDir.toString());

        Field timerSetField = LocalCQConfig.class.getDeclaredField("timerSet");
        timerSetField.setAccessible(true);
        timerSetField.set(localCQConfig, false);

        Field timerField = LocalCQConfig.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        timerField.set(localCQConfig, null);

        storedQueriesFilePath = tempDir.resolve("stored-queries.json");

        Files.createDirectories(storedQueriesFilePath.getParent());
        if (Files.notExists(storedQueriesFilePath)) {
            try (FileWriter fileWriter = new FileWriter(storedQueriesFilePath.toFile())) {
                fileWriter.write("{\"query\": \"select * from example\"}");
            }
        }
    }

    @AfterEach
    void tearDown() throws NoSuchFieldException, IllegalAccessException {
        Field timerField = LocalCQConfig.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        Timer timerInstance = (Timer) timerField.get(localCQConfig);
        if (timerInstance != null) {
            timerInstance.cancel();
        }
    }

    @Test
    void testInit_FileExistence() throws IOException {
        assertTrue(Files.exists(storedQueriesFilePath));
        String content = new String(Files.readAllBytes(storedQueriesFilePath));
        assertEquals("{\"query\": \"select * from example\"}", content);
    }

    @Test
    void testInit_FileNotFound() {
        try {
            Field storedQueriesLocationField = LocalCQConfig.class.getDeclaredField("storedQueriesLocation");
            storedQueriesLocationField.setAccessible(true);
            storedQueriesLocationField.set(localCQConfig, "invalid/path/to/stored-queries.json");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Error setting storedQueriesLocation");
        }
        assertDoesNotThrow(() -> localCQConfig.init());
    }

    @Test
    void testQueryConfigIsSet() throws IOException {
        localCQConfig.init();
        assertNotNull(localCQConfig.queryConfig);
    }

    @Test
    void testFileWatcherOnChange() throws InterruptedException, IOException {
        String newQuery = "{\"query\": \"select * from new_example\"}";
        try (FileWriter fileWriter = new FileWriter(storedQueriesFilePath.toFile())) {
            fileWriter.write(newQuery);
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    String content = new String(Files.readAllBytes(storedQueriesFilePath));
                    assertEquals(newQuery, content);
                } catch (IOException e) {
                    fail("Error reading the file during the file change test");
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 1000);
        Thread.sleep(2000);
    }

    @Test
    void testTimerTaskInitialization() throws NoSuchFieldException, IllegalAccessException {
        Field timerSetField = LocalCQConfig.class.getDeclaredField("timerSet");
        timerSetField.setAccessible(true);
        assertFalse((Boolean) timerSetField.get(localCQConfig));

        localCQConfig.init();

        assertTrue((Boolean) timerSetField.get(localCQConfig));
    }

    @Test
    void testFileWatcherIndirect() throws InterruptedException, IOException {
        String initialContent = "{\"query\": \"select * from example\"}";
        String updatedContent = "{\"query\": \"select * from modified_example\"}";

        try (FileWriter fileWriter = new FileWriter(storedQueriesFilePath.toFile())) {
            fileWriter.write(initialContent);
        }

        localCQConfig.init();

        try (FileWriter fileWriter = new FileWriter(storedQueriesFilePath.toFile())) {
            fileWriter.write(updatedContent);
        }

        TimerTask watcherTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    String content = new String(Files.readAllBytes(storedQueriesFilePath));
                    assertEquals(updatedContent, content);
                } catch (IOException e) {
                    fail("Error reading the file during file watcher indirect test");
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(watcherTask, 1000);
        Thread.sleep(2000);
    }

    @Test
    void testOnChange() throws Exception {
        LocalCQConfig.FileWatcher fileWatcher = spy(localCQConfig.new FileWatcher(new File(storedQueriesFilePath.toString())) {
            @Override
            protected void onChange(File var1) {
            }
        });

        String updatedContent = "{\"query\": \"select * from updated_example\"}";
        try (FileWriter fileWriter = new FileWriter(storedQueriesFilePath.toFile())) {
            fileWriter.write(updatedContent);
        }

        fileWatcher.run();

        String content = new String(Files.readAllBytes(storedQueriesFilePath));
        assertEquals(updatedContent, content);
    }
}
