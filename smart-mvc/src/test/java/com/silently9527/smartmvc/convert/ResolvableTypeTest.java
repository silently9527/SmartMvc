package com.silently9527.smartmvc.convert;

import org.junit.Test;
import org.springframework.core.ResolvableType;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ResolvableTypeTest {

    private HashMap<String, List<String>> map;

    private List<Set<Integer>> list;

    @Test
    public void testResolvableType() throws NoSuchFieldException {
        ResolvableType resolvableType = ResolvableType.forField(ResolvableTypeTest.class.getDeclaredField("map"));

        System.out.println(resolvableType.getSuperType()); //java.util.AbstractMap<java.lang.String, java.util.List<java.lang.String>>
        System.out.println(resolvableType.asMap()); //java.util.Map<java.lang.String, java.util.List<java.lang.String>>
        System.out.println(resolvableType.getGeneric(0).resolve()); //class java.lang.String
        System.out.println(resolvableType.getGeneric(1)); //java.util.List<java.lang.String>
        System.out.println(resolvableType.getGeneric(1).resolve()); //interface java.util.List
        System.out.println(resolvableType.getGenerics().length); //2
        Stream.of(resolvableType.getInterfaces()).forEach(System.out::println);
        //java.util.Map<java.lang.String, java.util.List<java.lang.String>>
        //java.lang.Cloneable
        //java.io.Serializable

        System.out.println(resolvableType.getRawClass()); //class java.util.HashMap

        System.out.println("------->");
        ResolvableType resolvableType1 = ResolvableType.forField(ResolvableTypeTest.class.getDeclaredField("list"));
        System.out.println(resolvableType1.getNested(1)); //java.util.List<java.util.Set<java.lang.Integer>>
        System.out.println(resolvableType1.getNested(2)); //java.util.Set<java.lang.Integer>
        System.out.println(resolvableType1.getNested(3)); //java.lang.Integer

    }
}
