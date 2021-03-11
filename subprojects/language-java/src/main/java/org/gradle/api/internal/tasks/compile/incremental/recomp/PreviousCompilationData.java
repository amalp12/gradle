/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks.compile.incremental.recomp;

import org.gradle.api.internal.cache.StringInterner;
import org.gradle.api.internal.tasks.compile.incremental.classpath.ClasspathSnapshotData;
import org.gradle.api.internal.tasks.compile.incremental.classpath.ClasspathSnapshotDataSerializer;
import org.gradle.api.internal.tasks.compile.incremental.processing.AnnotationProcessingData;
import org.gradle.internal.serialize.AbstractSerializer;
import org.gradle.internal.serialize.BaseSerializerFactory;
import org.gradle.internal.serialize.Decoder;
import org.gradle.internal.serialize.Encoder;

import java.io.File;

public class PreviousCompilationData {
    private final File destinationDir;
    private final AnnotationProcessingData annotationProcessingData;
    private final ClasspathSnapshotData classpathSnapshot;

    public PreviousCompilationData(File destinationDir, AnnotationProcessingData annotationProcessingData, ClasspathSnapshotData classpathSnapshot) {
        this.destinationDir = destinationDir;
        this.annotationProcessingData = annotationProcessingData;
        this.classpathSnapshot = classpathSnapshot;
    }

    public File getDestinationDir() {
        return destinationDir;
    }

    public AnnotationProcessingData getAnnotationProcessingData() {
        return annotationProcessingData;
    }

    public ClasspathSnapshotData getClasspathSnapshot() {
        return classpathSnapshot;
    }

    public static class Serializer extends AbstractSerializer<PreviousCompilationData> {
        private final ClasspathSnapshotDataSerializer classpathSnapshotDataSerializer;
        private final AnnotationProcessingData.Serializer annotationProcessingDataSerializer;

        public Serializer(StringInterner interner) {
            classpathSnapshotDataSerializer = new ClasspathSnapshotDataSerializer();
            annotationProcessingDataSerializer = new AnnotationProcessingData.Serializer(interner);
        }

        @Override
        public PreviousCompilationData read(Decoder decoder) throws Exception {
            File destinationDir = BaseSerializerFactory.FILE_SERIALIZER.read(decoder);
            ClasspathSnapshotData classpathSnapshot = classpathSnapshotDataSerializer.read(decoder);
            AnnotationProcessingData annotationProcessingData = annotationProcessingDataSerializer.read(decoder);
            return new PreviousCompilationData(destinationDir, annotationProcessingData, classpathSnapshot);
        }

        @Override
        public void write(Encoder encoder, PreviousCompilationData value) throws Exception {
            BaseSerializerFactory.FILE_SERIALIZER.write(encoder, value.destinationDir);
            classpathSnapshotDataSerializer.write(encoder, value.classpathSnapshot);
            annotationProcessingDataSerializer.write(encoder, value.annotationProcessingData);
        }
    }
}
