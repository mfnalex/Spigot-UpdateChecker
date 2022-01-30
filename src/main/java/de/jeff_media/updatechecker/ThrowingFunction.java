package de.jeff_media.updatechecker;

@FunctionalInterface
interface ThrowingFunction<T,R,E extends Exception> {
    R apply(T t) throws E;
}
