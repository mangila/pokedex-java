package com.github.mangila.pokedex.backstage.shared.config;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.ProtocolMessageEnum;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.List;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class ProtobufRuntimeHints implements RuntimeHintsRegistrar {

    private static final Logger log = LoggerFactory.getLogger(ProtobufRuntimeHints.class);
    private static final String REDIS_PROTOBUF_PACKAGE = "com.github.mangila.pokedex.backstage.model.grpc.redis";
    private static final String MONGO_DB_PROTOBUF_PACKAGE = "com.github.mangila.pokedex.backstage.model.grpc.mongodb";

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var packages = List.of(
                REDIS_PROTOBUF_PACKAGE, MONGO_DB_PROTOBUF_PACKAGE
        );
        for (String packageName : packages) {
            Reflections reflections = new Reflections(packageName);
            registerMessages(hints, reflections.getSubTypesOf(GeneratedMessageV3.class));
            registerBuilders(hints, reflections.getSubTypesOf(GeneratedMessageV3.Builder.class));
            registerEnums(hints, reflections.getSubTypesOf(ProtocolMessageEnum.class));
        }
    }

    private static void registerMessages(RuntimeHints hints, Set<Class<? extends GeneratedMessageV3>> messageClasses) {
        messageClasses.forEach(messageClass -> {
            log.info(messageClass.getName());
            hints.reflection()
                    .registerType(messageClass, builder -> builder
                            .withMembers(
                                    MemberCategory.INVOKE_DECLARED_METHODS
                            ));
        });
    }

    private static void registerBuilders(RuntimeHints hints, Set<Class<? extends GeneratedMessageV3.Builder>> builderClasses) {
        builderClasses.forEach(builderClass -> {
            log.info(builderClass.getName());
            hints.reflection()
                    .registerType(builderClass, builder -> builder
                            .withMembers(
                                    MemberCategory.INVOKE_DECLARED_METHODS
                            ));
        });
    }

    private static void registerEnums(RuntimeHints hints, Set<Class<? extends ProtocolMessageEnum>> enums) {
        enums.forEach(enumClass -> {
            log.info(enumClass.getName());
            hints.reflection()
                    .registerType(enumClass, builder -> builder
                            .withMembers(
                                    MemberCategory.INVOKE_DECLARED_METHODS
                            ));
        });
    }
}
