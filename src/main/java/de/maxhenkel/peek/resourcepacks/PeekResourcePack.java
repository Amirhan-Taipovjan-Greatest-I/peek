package de.maxhenkel.peek.resourcepacks;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import de.maxhenkel.peek.Peek;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PeekResourcePack extends AbstractPackResources {

    protected String path;
    protected Component name;

    public PeekResourcePack(String path, Component name) {
        super(null);
        this.path = path;
        this.name = name;
    }

    @Nullable
    public Pack toPack() {
        try {
            PackMetadataSection packMetadataSection = getMetadataSection(PackMetadataSection.SERIALIZER);
            if (packMetadataSection == null) {
                return null;
            }
            return new Pack(path, false, () -> this, name, packMetadataSection.getDescription(), PackCompatibility.forMetadata(packMetadataSection, PackType.CLIENT_RESOURCES), Pack.Position.TOP, false, PackSource.BUILT_IN);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return path;
    }

    private String getPath() {
        return "/packs/" + path + "/";
    }

    @Nullable
    private InputStream get(String name) {
        return Peek.class.getResourceAsStream(getPath() + name);
    }

    @Override
    protected InputStream getResource(String name) throws IOException {
        InputStream resourceAsStream = get(name);
        if (resourceAsStream == null) {
            throw new FileNotFoundException("Resource " + name + " does not exist");
        }
        return resourceAsStream;
    }

    @Override
    protected boolean hasResource(String name) {
        try {
            return get(name) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType type, String namespace, String prefix, Predicate<ResourceLocation> pathFilter) {
        List<ResourceLocation> list = Lists.newArrayList();
        try {
            URL url = Peek.class.getResource(getPath());
            if (url == null) {
                return list;
            }
            Path namespacePath = Paths.get(url.toURI()).resolve(type.getDirectory()).resolve(namespace);
            Path resPath = namespacePath.resolve(prefix);

            if (!Files.exists(resPath)) {
                return list;
            }

            try (Stream<Path> files = Files.walk(resPath)) {
                files.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                    ResourceLocation resourceLocation = new ResourceLocation(namespace, convertPath(path).substring(convertPath(namespacePath).length() + 1));
                    list.add(resourceLocation);
                });
            }
        } catch (Exception e) {
            Peek.LOGGER.error("Failed to list builtin pack resources", e);
        }
        return list.stream().filter(pathFilter).toList();
    }

    private static String convertPath(Path path) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < path.getNameCount(); i++) {
            stringBuilder.append(path.getName(i));
            if (i < path.getNameCount() - 1) {
                stringBuilder.append("/");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return ImmutableSet.of(ResourceLocation.DEFAULT_NAMESPACE);
    }

    @Override
    public void close() {

    }
}
