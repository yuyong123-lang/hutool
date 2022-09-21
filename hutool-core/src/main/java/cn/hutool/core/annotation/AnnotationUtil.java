package cn.hutool.core.annotation;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.map.WeakConcurrentMap;
import cn.hutool.core.reflect.FieldUtil;
import cn.hutool.core.reflect.MethodUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;

import java.lang.annotation.*;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 注解工具类<br>
 * 快速获取注解对象、注解值等工具封装
 *
 * @author looly
 * @since 4.0.9
 */
public class AnnotationUtil {

	/**
	 * 直接声明的注解缓存
	 */
	private static final Map<AnnotatedElement, Annotation[]> DECLARED_ANNOTATIONS_CACHE = new WeakConcurrentMap<>();

	/**
	 * 获取直接声明的注解，若已有缓存则从缓存中获取
	 *
	 * @param element {@link AnnotatedElement}
	 * @return 注解
	 * @since 6.0.0
	 */
	public static Annotation[] getDeclaredAnnotations(AnnotatedElement element) {
		return MapUtil.computeIfAbsent(DECLARED_ANNOTATIONS_CACHE, element, AnnotatedElement::getDeclaredAnnotations);
	}

	/**
	 * 将指定的被注解的元素转换为组合注解元素
	 *
	 * @param annotationEle 注解元素
	 * @return 组合注解元素
	 */
	public static CombinationAnnotationElement toCombination(final AnnotatedElement annotationEle) {
		if (annotationEle instanceof CombinationAnnotationElement) {
			return (CombinationAnnotationElement) annotationEle;
		}
		return new CombinationAnnotationElement(annotationEle);
	}

	/**
	 * 获取指定注解
	 *
	 * @param annotationEle   {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
	 * @param isToCombination 是否为转换为组合注解，组合注解可以递归获取注解的注解
	 * @return 注解对象
	 */
	public static Annotation[] getAnnotations(final AnnotatedElement annotationEle, final boolean isToCombination) {
		return getAnnotations(annotationEle, isToCombination, (Predicate<Annotation>) null);
	}

	/**
	 * 获取组合注解
	 *
	 * @param <T>            注解类型
	 * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
	 * @param annotationType 限定的
	 * @return 注解对象数组
	 * @since 5.8.0
	 */
	public static <T> T[] getCombinationAnnotations(final AnnotatedElement annotationEle, final Class<T> annotationType) {
		return getAnnotations(annotationEle, true, annotationType);
	}

	/**
	 * 获取指定注解
	 *
	 * @param <T>             注解类型
	 * @param annotationEle   {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
	 * @param isToCombination 是否为转换为组合注解，组合注解可以递归获取注解的注解
	 * @param annotationType  限定的
	 * @return 注解对象数组
	 * @since 5.8.0
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] getAnnotations(final AnnotatedElement annotationEle, final boolean isToCombination, final Class<T> annotationType) {
		final Annotation[] annotations = getAnnotations(annotationEle, isToCombination,
				(annotation -> null == annotationType || annotationType.isAssignableFrom(annotation.getClass())));

		final T[] result = ArrayUtil.newArray(annotationType, annotations.length);
		for (int i = 0; i < annotations.length; i++) {
			result[i] = (T) annotations[i];
		}
		return result;
	}

	/**
	 * 获取指定注解
	 *
	 * @param annotationEle   {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
	 * @param isToCombination 是否为转换为组合注解，组合注解可以递归获取注解的注解
	 * @param predicate       过滤器，{@link Predicate#test(Object)}返回{@code true}保留，否则不保留
	 * @return 注解对象
	 * @since 5.8.0
	 */
	public static Annotation[] getAnnotations(final AnnotatedElement annotationEle, final boolean isToCombination, final Predicate<Annotation> predicate) {
		if (null == annotationEle) {
			return null;
		}

		if (isToCombination) {
			if (null == predicate) {
				return toCombination(annotationEle).getAnnotations();
			}
			return CombinationAnnotationElement.of(annotationEle, predicate).getAnnotations();
		}

		final Annotation[] result = annotationEle.getAnnotations();
		if (null == predicate) {
			return result;
		}
		return ArrayUtil.filter(result, predicate);
	}

	/**
	 * 获取指定注解
	 *
	 * @param <A>            注解类型
	 * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
	 * @param annotationType 注解类型
	 * @return 注解对象
	 */
	public static <A extends Annotation> A getAnnotation(final AnnotatedElement annotationEle, final Class<A> annotationType) {
		return (null == annotationEle) ? null : toCombination(annotationEle).getAnnotation(annotationType);
	}

	/**
	 * 检查是否包含指定注解指定注解
	 *
	 * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
	 * @param annotationType 注解类型
	 * @return 是否包含指定注解
	 * @since 5.4.2
	 */
	public static boolean hasAnnotation(final AnnotatedElement annotationEle, final Class<? extends Annotation> annotationType) {
		return null != getAnnotation(annotationEle, annotationType);
	}

	/**
	 * 获取指定注解默认值<br>
	 * 如果无指定的属性方法返回null
	 *
	 * @param <T>            注解值类型
	 * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
	 * @param annotationType 注解类型
	 * @return 注解对象
	 * @throws UtilException 调用注解中的方法时执行异常
	 */
	public static <T> T getAnnotationValue(final AnnotatedElement annotationEle, final Class<? extends Annotation> annotationType) throws UtilException {
		return getAnnotationValue(annotationEle, annotationType, "value");
	}

	/**
	 * 获取指定注解属性的值<br>
	 * 如果无指定的属性方法返回null
	 *
	 * @param <T>            注解值类型
	 * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
	 * @param annotationType 注解类型
	 * @param propertyName   属性名，例如注解中定义了name()方法，则 此处传入name
	 * @return 注解对象
	 * @throws UtilException 调用注解中的方法时执行异常
	 */
	public static <T> T getAnnotationValue(final AnnotatedElement annotationEle, final Class<? extends Annotation> annotationType, final String propertyName) throws UtilException {
		final Annotation annotation = getAnnotation(annotationEle, annotationType);
		if (null == annotation) {
			return null;
		}

		final Method method = MethodUtil.getMethodOfObj(annotation, propertyName);
		if (null == method) {
			return null;
		}
		return MethodUtil.invoke(annotation, method);
	}

	/**
	 * 获取指定注解中所有属性值<br>
	 * 如果无指定的属性方法返回null
	 *
	 * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
	 * @param annotationType 注解类型
	 * @return 注解对象
	 * @throws UtilException 调用注解中的方法时执行异常
	 */
	public static Map<String, Object> getAnnotationValueMap(final AnnotatedElement annotationEle, final Class<? extends Annotation> annotationType) throws UtilException {
		final Annotation annotation = getAnnotation(annotationEle, annotationType);
		if (null == annotation) {
			return null;
		}

		final Method[] methods = MethodUtil.getMethods(annotationType, t -> {
			if (ArrayUtil.isEmpty(t.getParameterTypes())) {
				// 只读取无参方法
				final String name = t.getName();
				// 跳过自有的几个方法
				return (false == "hashCode".equals(name)) //
						&& (false == "toString".equals(name)) //
						&& (false == "annotationType".equals(name));
			}
			return false;
		});

		final HashMap<String, Object> result = new HashMap<>(methods.length, 1);
		for (final Method method : methods) {
			result.put(method.getName(), MethodUtil.invoke(annotation, method));
		}
		return result;
	}

	/**
	 * 获取注解类的保留时间，可选值 SOURCE（源码时），CLASS（编译时），RUNTIME（运行时），默认为 CLASS
	 *
	 * @param annotationType 注解类
	 * @return 保留时间枚举
	 */
	public static RetentionPolicy getRetentionPolicy(final Class<? extends Annotation> annotationType) {
		final Retention retention = annotationType.getAnnotation(Retention.class);
		if (null == retention) {
			return RetentionPolicy.CLASS;
		}
		return retention.value();
	}

	/**
	 * 获取注解类可以用来修饰哪些程序元素，如 TYPE, METHOD, CONSTRUCTOR, FIELD, PARAMETER 等
	 *
	 * @param annotationType 注解类
	 * @return 注解修饰的程序元素数组
	 */
	public static ElementType[] getTargetType(final Class<? extends Annotation> annotationType) {
		final Target target = annotationType.getAnnotation(Target.class);
		if (null == target) {
			return new ElementType[]{ElementType.TYPE, //
					ElementType.FIELD, //
					ElementType.METHOD, //
					ElementType.PARAMETER, //
					ElementType.CONSTRUCTOR, //
					ElementType.LOCAL_VARIABLE, //
					ElementType.ANNOTATION_TYPE, //
					ElementType.PACKAGE//
			};
		}
		return target.value();
	}

	/**
	 * 是否会保存到 Javadoc 文档中
	 *
	 * @param annotationType 注解类
	 * @return 是否会保存到 Javadoc 文档中
	 */
	public static boolean isDocumented(final Class<? extends Annotation> annotationType) {
		return annotationType.isAnnotationPresent(Documented.class);
	}

	/**
	 * 是否可以被继承，默认为 false
	 *
	 * @param annotationType 注解类
	 * @return 是否会保存到 Javadoc 文档中
	 */
	public static boolean isInherited(final Class<? extends Annotation> annotationType) {
		return annotationType.isAnnotationPresent(Inherited.class);
	}

	/**
	 * 设置新的注解的属性（字段）值
	 *
	 * @param annotation      注解对象
	 * @param annotationField 注解属性（字段）名称
	 * @param value           要更新的属性值
	 * @since 5.5.2
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void setValue(final Annotation annotation, final String annotationField, final Object value) {
		final Map memberValues = (Map) FieldUtil.getFieldValue(Proxy.getInvocationHandler(annotation), "memberValues");
		memberValues.put(annotationField, value);
	}

	/**
	 * 获取别名支持后的注解
	 *
	 * @param annotationEle  被注解的类
	 * @param annotationType 注解类型Class
	 * @param <T>            注解类型
	 * @return 别名支持后的注解
	 * @since 5.7.23
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotationAlias(final AnnotatedElement annotationEle, final Class<T> annotationType) {
		final T annotation = getAnnotation(annotationEle, annotationType);
		return (T) Proxy.newProxyInstance(annotationType.getClassLoader(), new Class[]{annotationType}, new AnnotationProxy<>(annotation));
	}

	/**
	 * 获取注解属性，若已有缓存则从缓存中获取
	 *
	 * @param annotationType 注解类型
	 * @return 注解属性
	 * @since 6.0.0
	 */
	public static Method[] getAnnotationAttributes(final Class<? extends Annotation> annotationType) {
		return Stream.of(MethodUtil.getDeclaredMethods(annotationType))
			.filter(AnnotationUtil::isAnnotationAttribute)
			.toArray(Method[]::new);
	}

	/**
	 * 该方法是否是注解属性，需要满足下述条件：
	 * <ul>
	 *     <li>不是{@link Object#equals(Object)}；</li>
	 *     <li>不是{@link Object#hashCode()}；</li>
	 *     <li>不是{@link Object#toString()}；</li>
	 *     <li>不是桥接方法；</li>
	 *     <li>不是合成方法；</li>
	 *     <li>不是静态方法；</li>
	 *     <li>是公共方法；</li>
	 *     <li>方法必须没有参数；</li>
	 *     <li>方法必须有返回值（返回值类型不为{@link Void}）；</li>
	 * </ul>
	 *
	 * @param attribute 方法对象
	 * @return 是否
	 * @since 6.0.0
	 */
	public static boolean isAnnotationAttribute(final Method attribute) {
		return !MethodUtil.isEqualsMethod(attribute)
			&& !MethodUtil.isHashCodeMethod(attribute)
			&& !MethodUtil.isToStringMethod(attribute)
			&& ArrayUtil.isEmpty(attribute.getParameterTypes())
			&& ObjUtil.notEquals(attribute.getReturnType(), Void.class)
			&& !Modifier.isStatic(attribute.getModifiers())
			&& Modifier.isPublic(attribute.getModifiers())
			&& !attribute.isBridge()
			&& !attribute.isSynthetic();
	}

}
