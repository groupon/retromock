package retromock.test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

/**
 * Locates a File.
 *
 * @since 2014-06-17
 */
public class FileLocator {

    /**
     * Walks through all paths in the {@code classpath} and returns a {@linkplain List} of all files that match the pattern.
     *
     * @param fileNameGlob Glob pattern for file name, e.g. *.txt
     * @return {@link java.util.List} of {@link java.nio.file.Path} objects for all matching files
     */
    public static List<Path> findAllInClasspath(final String fileNameGlob) {
       return findInClasspath(fileNameGlob, false);
    }

    /**
     * Walks through all paths in the {@code classpath} and returns a {@linkplain java.nio.file.Path}
     * of the first file that matches the pattern.
     *
     * @param fileNameGlob Glob pattern for file name, e.g. *.txt
     * @return {{@link java.nio.file.Path} object for the first matching files
     */
    public static Path findFirstInClasspath(final String fileNameGlob) {
        List<Path> result = findInClasspath(fileNameGlob, true);
        return result.isEmpty() ? null : result.get(0);
    }

    private static List<Path> findInClasspath(final String fileNameGlob, final boolean terminateOnFirstFind) {
        List<Path> result = new ArrayList<>();
        Path[] classpaths = classpaths();
        for (Path path : classpaths) {
            try {
                Files.walkFileTree(path, fileVisitor(glob(fileNameGlob), result, terminateOnFirstFind));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;

    }

    private static Path[] classpaths() {
        String[] parts = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        List<Path> paths = new ArrayList<>(parts.length);
        for (String part : parts) {
            paths.add(Paths.get(part));
        }
        return paths.toArray(new Path[paths.size()]);
    }

    private static String glob(String pattern) {
        return "glob:" + pattern;
    }

    private static String regex(String pattern) {
        return "regex:" + pattern;
    }

    private static FileVisitor<Path> fileVisitor(final String pattern, final Collection<Path> result, final boolean terminateOnFirstFind) {
        return new SimpleFileVisitor<Path>() {
            private final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path.getFileName())) {
                    result.add(path);
                    if (terminateOnFirstFind) {
                        return TERMINATE;
                    }
                }
                return CONTINUE;
            }
        };
    }
}
