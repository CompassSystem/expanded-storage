package compasses.expandedstorage.impl.block.misc;

public interface Property<A, B> {
    B get(A first, A second);

    B get(A single);
}
