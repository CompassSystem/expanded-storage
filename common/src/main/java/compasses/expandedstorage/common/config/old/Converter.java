package compasses.expandedstorage.common.config.old;

public interface Converter<S, T> {
    T fromSource(S source);

    S toSource(T target);

    int getSourceVersion();

    int getTargetVersion();
}
