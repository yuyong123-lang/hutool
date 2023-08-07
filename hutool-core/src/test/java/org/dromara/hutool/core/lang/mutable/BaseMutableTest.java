package org.dromara.hutool.core.lang.mutable;

import org.dromara.hutool.core.text.StrValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

/**
 * base test for {@link Mutable} implementations.
 *
 * @author huangchengxing
 */
abstract class BaseMutableTest<V, M extends Mutable<V>> {

	/**
	 * 创建一个值，且反复调用应该返回完全相同的值
	 *
	 * @return 值
	 */
	abstract V getValue1();

	/**
	 * 创建一个值，与{@link #getValue1()}不同，且反复调用应该返回完全相同的值
	 *
	 * @return 值
	 */
	abstract V getValue2();

	/**
	 * 创建一个{@link Mutable}
	 *
	 * @param value 值
	 * @return 值
	 */
	abstract M getMutable(V value);

	@Test
	void testOf() {
		Assertions.assertInstanceOf(MutableBool.class, Mutable.of(true));
		Assertions.assertInstanceOf(MutableByte.class, Mutable.of((byte) 1));
		Assertions.assertInstanceOf(MutableDouble.class, Mutable.of(1.0D));
		Assertions.assertInstanceOf(MutableFloat.class, Mutable.of(1.0F));
		Assertions.assertInstanceOf(MutableInt.class, Mutable.of(1));
		Assertions.assertInstanceOf(MutableLong.class, Mutable.of(1L));
		Assertions.assertInstanceOf(MutableObj.class, Mutable.of(new Object()));
		Assertions.assertInstanceOf(MutableShort.class, Mutable.of((short) 1));
	}

	@Test
	void testGet() {
		Mutable<V> mutableObj = getMutable(getValue1());
		V value = mutableObj.get();
		Assertions.assertEquals(getValue1(), value);
	}

	@Test
	void testSet() {
		Mutable<V> mutableObj = getMutable(getValue2());
		mutableObj.set(getValue2());
		V value = mutableObj.get();
		Assertions.assertEquals(getValue2(), value);
	}

	@Test
	void testTo() {
		Mutable<V> mutableObj = getMutable(getValue1());
		String value = mutableObj.to(String::valueOf);
		Assertions.assertEquals(String.valueOf(getValue1()), value);
	}

	@Test
	void testToOpt() {
		Mutable<V> mutableObj = getMutable(getValue1());
		V value = mutableObj.toOpt().get();
		Assertions.assertEquals(getValue1(), value);
	}

	@Test
	void testMap() {
		Mutable<V> mutableObj = getMutable(getValue1());
		V value = mutableObj.map(v -> getValue2()).get();
		Assertions.assertEquals(getValue2(), value);
	}

	@Test
	void testTest() {
		Mutable<V> mutableObj = getMutable(getValue1());
		Assertions.assertTrue(mutableObj.test(Objects::nonNull));
	}

	@Test
	void testPeek() {
		Mutable<V> m1 = getMutable(getValue1());
		Mutable<V> m2 = getMutable(getValue2());
		m1.peek(m2::set);
		Assertions.assertEquals(getValue1(), m2.get());
	}

	@Test
	void testHashCode() {
		V value = getValue1();
		Mutable<V> mutableObj = new MutableObj<>(value);
		Assertions.assertEquals(value.hashCode(), mutableObj.hashCode());
		mutableObj.set(null);
		Assertions.assertEquals(0, mutableObj.hashCode());
	}

	@Test
	void testEquals() {
		V value = getValue1();
		Mutable<V> mutableObj = new MutableObj<>(value);
		Assertions.assertNotEquals(value, mutableObj);
		Assertions.assertEquals(mutableObj, mutableObj);
		Assertions.assertEquals(mutableObj, new MutableObj<>(value));
		Assertions.assertNotEquals(mutableObj, new MutableObj<>(null));
		Assertions.assertNotEquals(mutableObj, null);
		Assertions.assertNotEquals(mutableObj, value);
	}

	@Test
	void testToString() {
		V value = getValue1();
		Mutable<V> mutableObj = new MutableObj<>(value);
		Assertions.assertEquals(value.toString(), mutableObj.toString());
		mutableObj.set(null);
		Assertions.assertEquals(StrValidator.NULL, mutableObj.toString());
	}
}