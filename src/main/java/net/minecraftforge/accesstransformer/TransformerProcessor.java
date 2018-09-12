package net.minecraftforge.accesstransformer;

import joptsimple.*;
import joptsimple.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.config.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class TransformerProcessor {

    private static final Logger LOGGER = LogManager.getLogger("AXFORM");
    private static final Marker AXFORM_MARKER = MarkerManager.getMarker("AXFORM");

    public static void main(String... args) {
        Configurator.initialize("", "atlog4j2.xml");
        final OptionParser optionParser = new OptionParser();
        final ArgumentAcceptingOptionSpec<Path> inputJar = optionParser.accepts("inJar", "Input JAR file to apply transformation to").withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING)).required();
        final ArgumentAcceptingOptionSpec<Path> atFiles = optionParser.acceptsAll(list("atfile", "atFile"), "Access Transformer File").withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING)).required();
        final ArgumentAcceptingOptionSpec<Path> outputJar = optionParser.accepts("outJar", "Output JAR file").withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.NOT_EXISTING));

        final OptionSet optionSet;
        Path inputJarPath;
        Path outputJarPath;
        List<Path> atFilePaths;
        try {
            optionSet = optionParser.parse(args);
            inputJarPath = inputJar.value(optionSet).toAbsolutePath();
            final String s = inputJarPath.getFileName().toString();
            outputJarPath = outputJar.value(optionSet);
            if (outputJarPath == null) {
                outputJarPath = inputJarPath.resolveSibling(s.substring(0,s.length()-4)+"-new.jar");
            } else {
                outputJarPath = outputJarPath.toAbsolutePath();
            }

            atFilePaths = atFiles.values(optionSet).stream().map(a -> a.toAbsolutePath()).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error(AXFORM_MARKER,"Option Parsing Error", e);
            return;
        }
        LOGGER.debug(AXFORM_MARKER,"Reading from {}", inputJarPath);
        LOGGER.debug(AXFORM_MARKER,"Writing to {}", outputJarPath);
        atFilePaths.forEach(path -> LOGGER.debug(AXFORM_MARKER,"Transform file {}", path));
        try {
            Files.deleteIfExists(outputJarPath);
        } catch (IOException e) {
            LOGGER.error(AXFORM_MARKER,"Deleting existing out JAR", e);
        }
        processJar(inputJarPath, outputJarPath, atFilePaths);
        LOGGER.debug(AXFORM_MARKER,"Transforming JAR complete {}", outputJarPath);
    }

    private static List<String> list(String... vars) {
        return Arrays.asList(vars);
    }

    private static void processJar(final Path inputJar, final Path outputJarPath, final List<Path> atFilePaths) {
        atFilePaths.forEach(path -> {
            AccessTransformerEngine.INSTANCE.addResource(path, path.getFileName().toString());
            LOGGER.debug(AXFORM_MARKER,"Loaded transformers {}", path);
        });

        try (FileOutputStream fos = new FileOutputStream(outputJarPath.toFile());
            ZipOutputStream zout = new ZipOutputStream(fos);
            ZipFile zin = new ZipFile(inputJar.toFile())) {
            for(Enumeration<? extends ZipEntry> enu = zin.entries(); enu.hasMoreElements();) {
                ZipEntry entry = enu.nextElement();
                if (entry.isDirectory()) {
                    continue; //IDGAF about directories
                }

                putEntry(zout, entry);
                if (entry.getName().endsWith(".class")) {
                    try (InputStream is = zin.getInputStream(entry)) {
                        final ClassReader classReader = new ClassReader(is);
                        final ClassNode cn = new ClassNode();
                        classReader.accept(cn, 0);
                        final Type type = Type.getType('L'+cn.name.replaceAll("\\.","/")+';');
                        if (AccessTransformerEngine.INSTANCE.handlesClass(type)) {
                            LOGGER.debug(AXFORM_MARKER,"Transforming class {}", type);
                            AccessTransformerEngine.INSTANCE.transform(cn, type);
                            ClassWriter cw = new ClassWriter(Opcodes.ASM5);
                            cn.accept(cw);
                            zout.write(cw.toByteArray());
                        } else {
                            LOGGER.debug(AXFORM_MARKER,"Skipping {}", type);
                            copy(zin.getInputStream(entry), zout);
                            zout.closeEntry();
                        }
                    }
                } else {
                    LOGGER.debug(AXFORM_MARKER,"Copying {}", entry.getName());
                    copy(zin.getInputStream(entry), zout);
                }
                zout.closeEntry();
            }
        } catch (IOException e) {
            LOGGER.error(AXFORM_MARKER,"Processing JAR", e);
        }
    }

    private static void putEntry(ZipOutputStream out, ZipEntry old) throws IOException {
        ZipEntry ent = new ZipEntry(old.getName());
        ent.setTime(0); //Stabilizes times
        out.putNextEntry(ent);
    }
    public static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buf = new byte[0x100];
        int n = 0;
        while ((n = input.read(buf)) != -1) {
            output.write(buf, 0, n);
        }
    }
}
