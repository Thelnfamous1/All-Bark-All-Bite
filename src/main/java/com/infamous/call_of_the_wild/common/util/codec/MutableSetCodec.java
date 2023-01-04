package com.infamous.call_of_the_wild.common.util.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public final class MutableSetCodec<A> implements Codec<Set<A>> {
    private final Codec<A> elementCodec;

    public MutableSetCodec(Codec<A> elementCodec) {
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> DataResult<T> encode(Set<A> input, DynamicOps<T> ops, T prefix) {
        ListBuilder<T> builder = new ListBuilder.Builder<>(ops);

        for (A a : input) {
            builder.add(this.elementCodec.encodeStart(ops, a));
        }

        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<Set<A>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap((stream) -> {
            Set<A> read = new HashSet<>();
            Stream.Builder<T> failed = Stream.builder();
            MutableObject<DataResult<Unit>> result = new MutableObject<>(DataResult.success(Unit.INSTANCE, Lifecycle.stable()));
            stream.accept((t) -> {
                DataResult<Pair<A, T>> element = this.elementCodec.decode(ops, t);
                element.error().ifPresent((e) -> failed.add(t));
                result.setValue(result.getValue().apply2stable((r, v) -> {
                    read.add(v.getFirst());
                    return r;
                }, element));
            });
            T errors = ops.createList(failed.build());
            Pair<Set<A>, T> pair = Pair.of(read, errors);
            return result.getValue().map((unit) -> pair).setPartial(pair);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            MutableSetCodec<?> mutableSetCodec = (MutableSetCodec<?>)o;
            return Objects.equals(this.elementCodec, mutableSetCodec.elementCodec);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.elementCodec);
    }

    @Override
    public String toString() {
        return "MutableSetCodec[" + this.elementCodec + "]";
    }
}