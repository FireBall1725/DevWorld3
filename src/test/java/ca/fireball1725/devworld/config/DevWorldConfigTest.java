package ca.fireball1725.devworld.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DevWorldConfigTest {
    @TempDir
    Path dir;

    private Path configDir() {
        return dir.resolve("config");
    }

    private Path configFile() {
        return configDir().resolve("devworld.json");
    }

    private void writeConfig(String json) throws IOException {
        Files.createDirectories(configDir());
        Files.writeString(configFile(), json);
    }

    private String externalUrl(String json) throws IOException {
        Path file = dir.resolve("external.json");
        Files.writeString(file, json);
        return file.toUri().toString();
    }

    @Test
    void writesTheDefaultsWhenThereIsNoFile() throws IOException {
        DevWorldConfig.load(configDir(), null);

        assertEquals("DevWorld", DevWorldConfig.world().worldName);
        assertEquals("creative", DevWorldConfig.world().gameMode);
        assertEquals(6000, DevWorldConfig.world().timeOfDay);
        assertFalse(DevWorldConfig.gameRules().get("doDaylightCycle").getAsBoolean());
        assertTrue(DevWorldConfig.gameRules().get("doMobSpawning").getAsBoolean());
        assertTrue(Files.readString(configFile()).contains("\"doDaylightCycle\": false"));
        assertNull(DevWorldConfig.externalHost());
        assertFalse(DevWorldConfig.externalFailed());
    }

    @Test
    void aPartialFileOnlyChangesWhatItSets() throws IOException {
        writeConfig("{\"world_config\": {\"seed\": 5}, \"game_rules\": {\"randomTickSpeed\": 7}}");

        DevWorldConfig.load(configDir(), null);

        assertEquals(5L, DevWorldConfig.world().seed);
        assertEquals("creative", DevWorldConfig.world().gameMode);
        assertEquals(7, DevWorldConfig.gameRules().get("randomTickSpeed").getAsInt());
        assertFalse(DevWorldConfig.gameRules().get("doDaylightCycle").getAsBoolean());
    }

    @Test
    void anOldFormatFileIsReadAndRewrittenWithTheNewNames() throws IOException {
        writeConfig("{\"game_rules\": {\"rule_dofiretick\": true, \"daylight_time\": 18000}}");

        DevWorldConfig.load(configDir(), null);

        assertTrue(DevWorldConfig.gameRules().get("doFireTick").getAsBoolean());
        assertEquals(18000, DevWorldConfig.world().timeOfDay);
        String written = Files.readString(configFile());
        assertTrue(written.contains("\"doFireTick\": true"));
        assertFalse(written.contains("rule_dofiretick"));
        assertFalse(written.contains("daylight_time"));
    }

    @Test
    void theExternalConfigSuppliesTheDefaults() throws IOException {
        String url = externalUrl(
                "{\"world_config\": {\"difficulty\": \"hard\"}, \"game_rules\": {\"rule_mobgriefing\": true}}");

        DevWorldConfig.load(configDir(), url);

        assertEquals("hard", DevWorldConfig.world().difficulty);
        assertTrue(DevWorldConfig.gameRules().get("mobGriefing").getAsBoolean());
        assertNotNull(DevWorldConfig.externalHost());
        assertFalse(DevWorldConfig.externalFailed());
    }

    @Test
    void theLocalFileWinsOverTheExternalConfig() throws IOException {
        writeConfig("{\"world_config\": {\"difficulty\": \"easy\"}}");
        String url = externalUrl("{\"world_config\": {\"difficulty\": \"hard\", \"seed\": 9}}");

        DevWorldConfig.load(configDir(), url);

        assertEquals("easy", DevWorldConfig.world().difficulty);
        assertEquals(9L, DevWorldConfig.world().seed);
    }

    @Test
    void anExternalConfigThatCannotBeLoadedFallsBackToTheDefaults() {
        DevWorldConfig.load(configDir(), dir.resolve("missing.json").toUri().toString());

        assertTrue(DevWorldConfig.externalFailed());
        assertNull(DevWorldConfig.externalHost());
        assertEquals("normal", DevWorldConfig.world().difficulty);
    }

    @Test
    void aFileThatDoesNotParseIsNotOverwritten() throws IOException {
        writeConfig("{ this is not json");

        DevWorldConfig.load(configDir(), null);

        assertEquals("{ this is not json", Files.readString(configFile()));
        assertEquals("DevWorld", DevWorldConfig.world().worldName);
    }

    @Test
    void theStateOfOneLoadDoesNotLeakIntoTheNext() throws IOException {
        DevWorldConfig.load(configDir(), dir.resolve("missing.json").toUri().toString());
        assertTrue(DevWorldConfig.externalFailed());

        DevWorldConfig.load(dir.resolve("other"), null);

        assertFalse(DevWorldConfig.externalFailed());
    }
}
