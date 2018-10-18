/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package jdk.internal;

import java.lang.annotation.*;

/**
 * The {@code @HotSpotIntrinsicCandidate} annotation is specific to the
 * HotSpot Virtual Machine. It indicates that an annotated method
 * may be (but is not guaranteed to be) intrinsified by the HotSpot VM. A method
 * is intrinsified if the HotSpot VM replaces the annotated method with hand-written
 * assembly and/or hand-written compiler IR -- a compiler intrinsic -- to improve
 * performance. The {@code @HotSpotIntrinsicCandidate} annotation is internal to the
 * Java libraries and is therefore not supposed to have any relevance for application
 * code.
 * @HotSpotIntrinsicCandidate 该注解是特定于Java虚拟机的注解。
 * 被注解的方法可能（但不并一定）由HotSpot虚拟机替换为汇编或IR编译器来提高性能。
 * @HotSpotIntrinsicCandidate 注解在Java库中用于内部实现，因此不建议在应用代码中使用。
 *
 * Maintainers of the Java libraries must consider the following when
 * modifying methods annotated with {@code @HotSpotIntrinsicCandidate}.
 * Java库的开发者在修改被@HotSpotIntrinsicCandidate注解的方法时，必须考虑以下内容。
 *
 * <ul>
 * <li>When modifying a method annotated with {@code @HotSpotIntrinsicCandidate},
 * the corresponding intrinsic code in the HotSpot VM implementation must be
 * updated to match the semantics of the annotated method.</li>
 * 当修改被注解@HotSpotIntrinsicCandidate的方法时，相应HotSpot JVM实现的内在代码也必须同步更新。
 *
 * <li>For some annotated methods, the corresponding intrinsic may omit some low-level
 * checks that would be performed as a matter of course if the intrinsic is implemented
 * using Java bytecodes. This is because individual Java bytecodes implicitly check
 * for exceptions like {@code NullPointerException} and {@code ArrayStoreException}.
 * If such a method is replaced by an intrinsic coded in assembly language, any
 * checks performed as a matter of normal bytecode operation must be performed
 * before entry into the assembly code. These checks must be performed, as
 * appropriate, on all arguments to the intrinsic, and on other values (if any) obtained
 * by the intrinsic through those arguments. The checks may be deduced by inspecting
 * the non-intrinsic Java code for the method, and determining exactly which exceptions
 * may be thrown by the code, including undeclared implicit {@code RuntimeException}s.
 * Therefore, depending on the data accesses performed by the intrinsic,
 * the checks may include:
 *
 * 对于一些被注解的方法，如果使用Java字节码来实现内在代码，JVM可能会忽略一些低级别的检查，这是因为Java字节码已经默认检查了
 * NullPointerException与ArrayStoreException。如果同样的方法被使用汇编实现内在代码化，则进入汇编代码前，必须要确保行为
 * 正常。必须对进入内在代码的所有参数进行检查，这包含直接调用传入的参数，也包含内在代码中直接获取的参数。这些检查也可能会被未
 * 内在化的Java代码推断，包含隐式的RuntimeException。因此，对于内在代码的数据访问操作，需要做检查有：
 *
 *  <ul>
 *  <li>null checks on references</li>
 *  引用是否为空的检查
 *  <li>range checks on primitive values used as array indexes</li>
 *  数组索引的范围检查
 *  <li>other validity checks on primitive values (e.g., for divide-by-zero conditions)</li>
 *  其它基本数据类型值的有效率检查（比如除数不能为0）
 *  <li>store checks on reference values stored into arrays</li>
 *  对数据中引用类型值进行存储检查（？）
 *  <li>array length checks on arrays indexed from within the intrinsic</li>
 *  内在代码中索引的数组长度检查
 *  <li>reference casts (when formal parameters are {@code Object} or some other weak type)</li>
 *  引用转换检查（当参数是Object或其它弱类型）
 *  </ul>
 *
 * </li>
 *
 *
 * <li>Note that the receiver value ({@code this}) is passed as a extra argument
 * to all non-static methods. If a non-static method is an intrinsic, the receiver
 * value does not need a null check, but (as stated above) any values loaded by the
 * intrinsic from object fields must also be checked. As a matter of clarity, it is
 * better to make intrinisics be static methods, to make the dependency on {@code this}
 * clear. Also, it is better to explicitly load all required values from object
 * fields before entering the intrinsic code, and pass those values as explicit arguments.
 * First, this may be necessary for null checks (or other checks). Second, if the
 * intrinsic reloads the values from fields and operates on those without checks,
 * race conditions may be able to introduce unchecked invalid values into the intrinsic.
 * If the intrinsic needs to store a value back to an object field, that value should be
 * returned explicitly from the intrinsic; if there are multiple return values, coders
 * should consider buffering them in an array. Removing field access from intrinsics
 * not only clarifies the interface with between the JVM and JDK; it also helps decouple
 * the HotSpot and JDK implementations, since if JDK code before and after the intrinsic
 * manages all field accesses, then intrinsics can be coded to be agnostic of object
 * layouts.</li>
 *
 * 注意，对于所有非static方法，this引用是做为一个特殊的参数传递。如果一个非静态方法是一个内在方法，方法不需要对this进行非空判断，但
 * 通过this引用加载的其它成员属性进行检查（上面已经提到）。为清楚起见，最好确保内在方法是静态方法，来确保不依赖this。同时，最好在进入
 * 内在代码前，从成员变量加载所有需要的值，做为参数，显式地传递给内在代码。首先，这可能需要进行空指针检查(或其它检查)。其次，如果内存
 * 代码如果没有检查，从字段和操作上重新加载值，竞态条件可能导致未检查的无效值进入内在代码。如果内在代码需要赋值给一个对象的字段，这个值
 * 应做为返回值从内在代码块返回；如果需要有多个返回值，编码者可以考虑用数组来缓存。从内在代码移除字段访问权限，不仅可以清楚JVM与JDK之间
 * 的接口，也可以解耦HotSpot与JDK实现，由于JDK代码前后是内在代码管理所有的字段访问，因此内在代码可以实现为与对象布局无关。
 *
 * Maintainers of the HotSpot VM must consider the following when modifying
 * intrinsics.
 *
 * HotSpot虚拟机的开发者在修改内在代码时，必须考虑以下内容。
 *
 * <ul>
 * <li>When adding a new intrinsic, make sure that the corresponding method
 * in the Java libraries is annotated with {@code @HotSpotIntrinsicCandidate}
 * and that all possible call sequences that result in calling the intrinsic contain
 * the checks omitted by the intrinsic (if any).</li>
 * 当引入一个新的内在代码时，确保Java库中对应的方法被注解为@HotSpotIntrinsicCandidate以及所有需要做的调用检查（如果有）。
 *
 * <li>When modifying an existing intrinsic, the Java libraries must be updated
 * to match the semantics of the intrinsic and to execute all checks omitted
 * by the intrinsic (if any).</li>
 * </ul>
 * 当修改一个存在的内在代码时，Java库必须同步更新成自己符合intrinsic语义，且必须执行所有被intrinsic忽略的检查（如果有）。
 *
 * Persons not directly involved with maintaining the Java libraries or the
 * HotSpot VM can safely ignore the fact that a method is annotated with
 * {@code @HotSpotIntrinsicCandidate}.
 * 不参与Java库或HotSpot VM维护的用户可以直接忽略方法上是否含有 @HotSpotIntrinsicCandidate 注解。
 *
 *
 * The HotSpot VM defines (internally) a list of intrinsics. Not all intrinsic
 * are available on all platforms supported by the HotSpot VM. Furthermore,
 * the availability of an intrinsic on a given platform depends on the
 * configuration of the HotSpot VM (e.g., the set of VM flags enabled).
 * Therefore, annotating a method with {@code @HotSpotIntrinsicCandidate} does
 * not guarantee that the marked method is intrinsified by the HotSpot VM.
 *
 * HotSpot VM内部定义了一个intrinsic列表。在HotSpot VM上，不是所有的intrinsic在所有平台上都是可用的。更进一步，一个intrinsic在给定
 * 平台的可用性取决于HotSpot VM的参数配置。因此，注解一个方法@HotSpotIntrinsicCandidate 并不能保证被标注的方法会被内在化。
 *
 * If the {@code CheckIntrinsics} VM flag is enabled, the HotSpot VM checks
 * (when loading a class) that (1) all methods of that class that are also on
 * the VM's list of intrinsics are annotated with {@code @HotSpotIntrinsicCandidate}
 * and that (2) for all methods of that class annotated with
 * {@code @HotSpotIntrinsicCandidate} there is an intrinsic in the list.
 *
 * 如果CheckIntrinsics的VM参数启用，HotSpot VM在加载类时，会检查两点：
 * 1. 被注解为@HotSpotIntrinsicCandidate的类中的方法，而且方法也在VM的intrinsic的列表
 * 2. 类中的方法被注解为@HotSpotIntrinsicCandidate，且方法也在VM的intrinsic的列表
 *
 * @since 9
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface HotSpotIntrinsicCandidate {
}
