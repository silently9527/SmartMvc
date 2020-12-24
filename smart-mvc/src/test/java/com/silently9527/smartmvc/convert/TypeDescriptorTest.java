package com.silently9527.smartmvc.convert;

import org.junit.Test;
import org.springframework.core.convert.TypeDescriptor;

import java.util.List;

public class TypeDescriptorTest {
    private List<String> list;


    @Test
    public void testTypeDescriptor() throws NoSuchFieldException {
        TypeDescriptor descriptor = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class));

        print(descriptor);
        System.out.println("=======>");
        print(new TypeDescriptor(TypeDescriptorTest.class.getDeclaredField("list")));
    }


    private void print(TypeDescriptor descriptor) {
        System.out.println("name: " + descriptor.getName());
        System.out.println("elementType: " + descriptor.getElementTypeDescriptor().getName());
    }

}
