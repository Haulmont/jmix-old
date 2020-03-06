/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.gradle;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class MetaModelUtil {
    public static final String ENTITY_TYPE = "io.jmix.core.entity.Entity";
    public static final String GENERIC_ENTITY_TYPE = "io.jmix.core.entity.ManagedEntity";
    public static final String ENTITY_ENTRY_TYPE = "io.jmix.core.entity.ManagedEntityEntry";
    public static final String BASE_ENTITY_ENTRY_TYPE = "io.jmix.core.entity.BaseManagedEntityEntry";
    public static final String OBJECT_TYPE = "java.lang.Object";
    public static final String STRING_TYPE = "java.lang.String";

    public static final String SETTERS_ENHANCED_TYPE = "io.jmix.core.entity.JmixSettersEnhanced";
    public static final String ENTITY_ENTRY_ENHANCED_TYPE = "io.jmix.core.entity.JmixEntityEntryEnhanced";

    public static final String TRANSIENT_ANNOTATION_TYPE = "javax.persistence.Transient";
    public static final String META_PROPERTY_ANNOTATION_TYPE = "io.jmix.core.metamodel.annotations.MetaProperty";
    public static final String DISABLE_ENHANCING_ANNOTATION_TYPE = "io.jmix.core.entity.annotations.DisableEnhancing";
    public static final String META_CLASS_ANNOTATION_TYPE = "io.jmix.core.metamodel.annotations.MetaClass";
    public static final String ENTITY_ANNOTATION_TYPE = "javax.persistence.Entity";
    public static final String EMBEDDABLE_ANNOTATION_TYPE = "javax.persistence.Embeddable";

    public static final String GET_ENTITY_ENTRY_METHOD_NAME = "__getEntityEntry";
    public static final String COPY_ENTITY_ENTRY_METHOD_NAME = "__copyEntityEntry";
    public static final String WRITE_OBJECT_METHOD_NAME = "writeObject";


    public static final String GEN_ENTITY_ENTRY_VAR_NAME = "_jmixEntityEntry";
    public static final String GEN_ENTITY_ENTRY_CLASS_NAME = "JmixEntityEntry";

    public static boolean isSettersEnhanced(CtClass ctClass) throws NotFoundException {
        for (CtClass ctInterface : ctClass.getInterfaces()) {
            if (Objects.equals(ctInterface.getName(), SETTERS_ENHANCED_TYPE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEntityEntryEnhanced(CtClass ctClass) throws NotFoundException {
        for (CtClass ctInterface : ctClass.getInterfaces()) {
            if (Objects.equals(ctInterface.getName(), ENTITY_ENTRY_ENHANCED_TYPE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasGenericEntityInterface(CtClass ctClass) throws NotFoundException {
        for (CtClass ctInterface : ctClass.getInterfaces()) {
            if (Objects.equals(ctInterface.getName(), GENERIC_ENTITY_TYPE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEnhancingDisabled(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null && attribute.getAnnotation(DISABLE_ENHANCING_ANNOTATION_TYPE) != null;
    }

    public static boolean subtypeOfEntityInterface(CtClass ctClass, ClassPool pool) throws NotFoundException {
        return ctClass.subtypeOf(pool.get(ENTITY_TYPE));
    }

    public static boolean isJpaEntity(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null && attribute.getAnnotation(ENTITY_ANNOTATION_TYPE) != null;
    }

    public static boolean isJpaEmbeddable(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null && attribute.getAnnotation(EMBEDDABLE_ANNOTATION_TYPE) != null;
    }

    public static boolean isJpaMappedSuperclass(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null && attribute.getAnnotation("javax.persistence.MappedSuperclass") != null;
    }

    public static boolean isMetaClass(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null && attribute.getAnnotation(META_CLASS_ANNOTATION_TYPE) != null;
    }

    public static CtField getPrimaryKey(CtClass ctClass) {
        for (CtField field : ctClass.getDeclaredFields()) {
            AnnotationsAttribute annotationsInfo = (AnnotationsAttribute) field.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);

            if (annotationsInfo == null) {
                continue;
            }

            if (annotationsInfo.getAnnotation("javax.persistence.Id") != null ||
                    annotationsInfo.getAnnotation("javax.persistence.EmbeddedId") != null) {
                return field;
            }

        }
        return null;
    }

    public static boolean isPersistentMethod(CtMethod ctMethod) {
        return ctMethod.getName().startsWith("_persistence_get_") ||
                ctMethod.getName().startsWith("_persistence_set_");
    }

    public static boolean isPersistentField(CtClass ctClass, String fieldName) {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (Objects.equals(method.getName(), "_persistence_set_" + fieldName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMetaPropertyField(CtClass ctClass, String fieldName) {
        CtField ctField = findDeclaredField(ctClass, fieldName);
        return ctField != null && hasAnnotationOnField(ctField, META_PROPERTY_ANNOTATION_TYPE);
    }

    public static boolean isSetterMethod(CtMethod ctMethod) throws NotFoundException {
        return !Modifier.isAbstract(ctMethod.getModifiers())
                && ctMethod.getName().startsWith("set")
                && ctMethod.getReturnType() == CtClass.voidType
                && ctMethod.getParameterTypes().length == 1;
    }

    public static String generateFieldNameByMethod(String methodName) {
        return StringUtils.uncapitalize(methodName.substring(3));
    }

    public static CtField findDeclaredField(CtClass ctClass, String fieldName) {
        for (CtField field : ctClass.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    public static CtMethod findEqualsMethod(CtClass ctClass) throws NotFoundException {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if ("equals".equals(method.getName())) {
                if (CtClass.booleanType.equals(method.getReturnType())
                        && method.getParameterTypes().length == 1
                        && OBJECT_TYPE.equals(method.getParameterTypes()[0].getName())) {
                    return method;
                }
            }
        }
        return null;
    }

    public static CtMethod findHashCodeMethod(CtClass ctClass) throws NotFoundException {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if ("hashCode".equals(method.getName())) {
                if (CtClass.intType.equals(method.getReturnType())
                        && method.getParameterTypes().length == 0) {
                    return method;
                }
            }
        }
        return null;
    }

    public static CtMethod findToStringMethod(CtClass ctClass) throws NotFoundException {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if ("toString".equals(method.getName())) {
                if (String.class.getName().equals(method.getReturnType().getName())
                        && method.getParameterTypes().length == 0) {
                    return method;
                }
            }
        }
        return null;
    }

    public static CtMethod findWriteObjectMethod(CtClass ctClass) throws NotFoundException {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (WRITE_OBJECT_METHOD_NAME.equals(method.getName())) {
                if (CtClass.voidType.equals(method.getReturnType())
                        && method.getParameterTypes().length == 1) {
                    return method;
                }
            }
        }
        return null;
    }

    public static boolean hasAnnotationOnField(CtField ctField, String annotationType) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctField.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
        return annotationsAttribute != null && annotationsAttribute.getAnnotation(annotationType) != null;
    }
}
