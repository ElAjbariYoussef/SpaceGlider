import java.io.*;
import java.nio.file.*;
import java.util.function.LongSupplier;

public class Extract {

    public static void extract(String spacePath, String outputDir) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(spacePath));
            int[] position = {0};
            LongSupplier get = () -> {
                int first = data[position[0]++] & 0xFF;

                if (first == 0) throw new IllegalStateException("corrupted .space file");

                int n = Integer.numberOfLeadingZeros(first) - 24 + 1;
                long value = first & ((1 << (8 - n)) - 1);

                for (int i = 1; i < n; i++) value = (value << 8) | (data[position[0]++] & 0xFF);
                for (int k = 1; k < n; k++) value += (1L << (7 * k));

                return value;
            };
            int dictSize = (int) get.getAsLong();
            byte[][] dict = new byte[dictSize][];

            for (int i = 0; i < dictSize; i++) {
                int len = (int) get.getAsLong();
                dict[i] = new byte[len];
                System.arraycopy(data, position[0], dict[i], 0, len);
                position[0] += len;
            }

            long tokenCount = get.getAsLong();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            for (long t = 0; t < tokenCount; t++) {
                byte[] word = dict[(int) get.getAsLong()];
                out.write(word, 0, word.length);
            }

            String name = Paths.get(spacePath).getFileName().toString();

            if (name.endsWith(".space")) name = name.substring(0, name.length() - 6);
            
            Path outPath = Paths.get(outputDir, name + "_extracted.txt");
            Files.createDirectories(outPath.getParent());
            Files.write(outPath, out.toByteArray());

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}