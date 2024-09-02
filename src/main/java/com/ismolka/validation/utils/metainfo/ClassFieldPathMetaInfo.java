package com.ismolka.validation.utils.metainfo;

public interface ClassFieldPathMetaInfo<T extends ClassFieldMetaInfo> {

    T getLast();

    Object getValueFromObject(Object object);
}
