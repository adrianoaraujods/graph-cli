package graph.cli.read;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class ReadHandlerIntegrationTest {

    @Test
    void testReadWeightedGraphWithTarjan() {
        ReadConfig config = new ReadConfig(
                "src/test/resources/weighted.txt",
                "forwardstar",
                false, // undirected
                true, // weighted
                false, // hasCapacity
                List.of(AlgorithmRequest.tarjan()),
                null);

        assertDoesNotThrow(() -> {
            List<AlgorithmOutput> outputs = ReadHandler.run(config);
            assertNotNull(outputs);
            assertEquals(1, outputs.size());
        });
    }

    @Test
    void testReadWeightedGraphWithNaiveGlobal() {
        ReadConfig config = new ReadConfig(
                "src/test/resources/weighted.txt",
                "forwardstar",
                false, // undirected
                true, // weighted
                false, // hasCapacity
                List.of(AlgorithmRequest.naiveBridges()),
                null);

        assertDoesNotThrow(() -> {
            List<AlgorithmOutput> outputs = ReadHandler.run(config);
            assertNotNull(outputs);
            assertEquals(1, outputs.size());
        });
    }

    @Test
    void testReadWeightedGraphWithFleury() {
        ReadConfig config = new ReadConfig(
                "src/test/resources/weighted.txt",
                "forwardstar",
                false, // undirected
                true, // weighted
                false, // hasCapacity
                List.of(AlgorithmRequest.fleury(null)),
                null);

        assertDoesNotThrow(() -> {
            List<AlgorithmOutput> outputs = ReadHandler.run(config);
            assertNotNull(outputs);
            assertEquals(1, outputs.size());
        });
    }

    @Test
    void testBackwardCompatibilityUnweighted() {
        ReadConfig config = new ReadConfig(
                "src/test/resources/weighted.txt",
                "forwardstar",
                false, // undirected
                false, // unweighted (should still work)
                false, // hasCapacity
                List.of(AlgorithmRequest.tarjan()),
                null);

        assertDoesNotThrow(() -> {
            List<AlgorithmOutput> outputs = ReadHandler.run(config);
            assertNotNull(outputs);
        });
    }
}
