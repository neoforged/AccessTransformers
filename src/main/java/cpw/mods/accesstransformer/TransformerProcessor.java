package cpw.mods.accesstransformer;

import joptsimple.*;
import joptsimple.util.*;
import org.apache.logging.log4j.core.config.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static cpw.mods.accesstransformer.Logging.log;

public class TransformerProcessor {
    public static void main(String... args) {
        Configurator.initialize("", "atlog4j2.xml");
        final OptionParser optionParser = new OptionParser();
        final ArgumentAcceptingOptionSpec<Path> inputJar = optionParser.accepts("inJar", "Input JAR file to apply transformation to").withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING)).required();
        final ArgumentAcceptingOptionSpec<Path> atFile = optionParser.accepts("atfile", "Access Transformer File").withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING)).required();

        final OptionSet optionSet;
        Path inputJarPath;
        Path outputJarPath;
        Path atFilePath;
        try {
            optionSet = optionParser.parse(args);
            inputJarPath = inputJar.value(optionSet).toAbsolutePath();
            final String s = inputJarPath.getFileName().toString();
            outputJarPath = inputJarPath.resolveSibling(s.substring(0,s.length()-4)+"-new.jar");
            atFilePath = atFile.value(optionSet).toAbsolutePath();
        } catch (Exception e) {
            log.error("Option Parsing Error", e);
            return;
        }
        log.info("Reading from {}", inputJarPath);
        log.info("Writing to {}", outputJarPath);
        log.info("Transform file {}", atFilePath);
        try {
            Files.deleteIfExists(outputJarPath);
        } catch (IOException e) {
            log.error("Deleting existing out JAR", e);
        }
        processJar(inputJar, atFile, optionSet, outputJarPath, atFilePath);
        log.info("Transforming JAR complete {}", outputJarPath);
    }

    private static void processJar(final ArgumentAcceptingOptionSpec<Path> inputJar, final ArgumentAcceptingOptionSpec<Path> atFile, final OptionSet optionSet, final Path outputJarPath, final Path atFilePath) {
        AccessTransformerEngine.INSTANCE.addResource(atFile.value(optionSet), "input");
        log.info("Loaded transformers {}", atFilePath);
        final URI outJarURI = URI.create("jar:file:" + outputJarPath);
        try (FileSystem outJar = FileSystems.newFileSystem(outJarURI, new HashMap<String, String>() {{
            put("create", "true");
        }})) {
            final Path outRoot = StreamSupport.stream(outJar.getRootDirectories().spliterator(), false).findFirst().get();
            try (FileSystem jarFile = FileSystems.newFileSystem(inputJar.value(optionSet), ClassLoader.getSystemClassLoader())) {
                Files.walk(StreamSupport.stream(jarFile.getRootDirectories().spliterator(), false).findFirst().orElseThrow(() -> new IllegalArgumentException("The JAR has no root?!")))
                        .forEach(path -> {
                            Path outPath = outJar.getPath(path.toAbsolutePath().toString());
                            if (Files.isDirectory(path)) {
                                try {
                                    Files.createDirectory(outPath);
                                } catch (IOException e) {
                                    // spammy
                                }
                            }
                            if (path.getNameCount() > 0 && path.getFileName().toString().endsWith(".class")) {
                                try (InputStream is = Files.newInputStream(path)) {
                                    final ClassReader classReader = new ClassReader(is);
                                    final ClassNode cn = new ClassNode();
                                    classReader.accept(cn, 0);
                                    final Type type = Type.getType('L'+cn.name.replaceAll("\\.","/")+';');
                                    if (AccessTransformerEngine.INSTANCE.handlesClass(type)) {
                                        log.debug("Transforming class {}", type);
                                        AccessTransformerEngine.INSTANCE.transform(cn, type);
                                        ClassWriter cw = new ClassWriter(Opcodes.ASM5);
                                        cn.accept(cw);
                                        Files.write(outPath, cw.toByteArray());
                                    } else {
                                        log.debug("Skipping {}", type);
                                        Files.copy(path, outPath);
                                    }
                                } catch (IOException e) {
                                    log.error("Reading {}", path, e);
                                }
                            } else if (!Files.exists(outPath)){
                                try {
                                    Files.copy(path, outPath);
                                } catch (IOException e) {
                                    log.error("Copying {}", path, e);
                                }
                            }
                        });
            } catch (IOException e) {
                log.error("Reading JAR", e);
            }
        } catch (IOException e) {
            log.error("Writing JAR", e);
        }
    }
}
