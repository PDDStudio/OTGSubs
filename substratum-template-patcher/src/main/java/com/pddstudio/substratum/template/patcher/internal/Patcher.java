package com.pddstudio.substratum.template.patcher.internal;

/**
 * Created by pddstudio on 25/04/2017.
 */

public interface Patcher<F, K, V> {
	F patch(F target, K key, V value);
}
