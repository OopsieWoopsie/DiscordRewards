package eu.mcdb.discordrewards.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

// TODO: javadoc, move to Spicord
public class ZipExtractor implements AutoCloseable {

    private final JarFile zipFile;
    private final List<ZipEntry> entries;

    /**
     * 
     * @param file the zip file
     * @throws IOException if an I/O error has occurred
     */
    public ZipExtractor(File file) throws IOException {
        this.zipFile = new JarFile(file);
        this.entries = new ArrayList<ZipEntry>();

        final Enumeration<JarEntry> e = zipFile.entries();

        while (e.hasMoreElements()) {
            entries.add(e.nextElement());
        }
    }

    /**
     * 
     * @param regex
     */
    public void filter(String regex) {
        final Pattern pattern = Pattern.compile(regex);
        entries.removeIf(entry -> !pattern.matcher(entry.getName()).find());
    }

    /**
     * 
     * @param out the output folder to extract the file
     * @throws IOException if an I/O error has occurred
     */
    public void extract(File out) throws IOException {
        for (final ZipEntry entry : entries) {
            String name = entry.getName();
            name = name.substring(name.lastIndexOf("/") + 1);

            File file = new File(out, name);

            if (!file.exists()) {
                file.createNewFile();
                Files.copy(zipFile.getInputStream(entry), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @Override
    public void close() throws Exception {
        entries.clear();
        zipFile.close();
    }
}
