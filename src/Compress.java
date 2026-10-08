import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.LongConsumer;

public class Compress {

    public static void compress(String filepath, String outputDir) {
        try {
            String text = new String(
                Files.readAllBytes(Paths.get(filepath)),
                StandardCharsets.UTF_8
            ); 
            String[] tokens = text.split("(?<=\\s)|(?=\\s)");
            Map<String, Integer> freq = new HashMap<>();
            for (String t : tokens) {
                freq.merge(t, 1, Integer::sum);
            }
            List<String> dict = new ArrayList<>(freq.keySet());
            dict.sort((a, b) -> {
                int scoreA = freq.get(a) * a.getBytes(StandardCharsets.UTF_8).length;
                int scoreB = freq.get(b) * b.getBytes(StandardCharsets.UTF_8).length;

                int c = Integer.compare(scoreB, scoreA);
                return c != 0 ? c : a.compareTo(b);
            });
            Map<String, Integer> rank = new HashMap<>();
            for (int i = 0; i < dict.size(); i++) {
                rank.put(dict.get(i), i);
            }

            //    n=1: 1xxxxxxx ; n=2: 01xxxxxx xxxxxxxx ; n=3: 001xxxxx xxxxxxxx xxxxxxxx, n is how many byte the word will take in compression, a high count word will take a low count of bytes
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            LongConsumer put = value -> {
                long offset = value;
                int n = 1;
                while (offset >= (1L << (7 * n))) {
                    offset -= (1L << (7 * n));
                    n++;
                }
                long code = (1L << (7 * n)) | offset;
                for (int i = n - 1; i >= 0; i--) buf.write((int) (code >>> (8 * i)) & 0xFF);
            };

            put.accept(dict.size());
            for (String w : dict) {
                byte[] b = w.getBytes(StandardCharsets.UTF_8);
                put.accept(b.length);
                buf.write(b, 0, b.length);
            }
            put.accept(tokens.length);
            for (String t : tokens) put.accept(rank.get(t));

            // out
            String name = Paths.get(filepath).getFileName().toString().replace('.', '_') + ".space";
            Path out = Paths.get(outputDir, name);
            Files.createDirectories(out.getParent());
            Files.write(out, buf.toByteArray());

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}