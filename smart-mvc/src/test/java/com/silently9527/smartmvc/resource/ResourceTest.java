package com.silently9527.smartmvc.resource;

import com.silently9527.smartmvc.vo.Customer;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.averagingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.minBy;
import static java.util.stream.Collectors.toList;

public class ResourceTest {

    @Test
    public void testByteArrayResource() throws IOException {
        String message = "ByteArrayResource";
        ByteArrayResource resource = new ByteArrayResource(message.getBytes());
        Assert.assertEquals(17, resource.contentLength()); //字节长度
    }

    @Test
    public void getGt5Data() throws IOException {
        List<Integer> numbers = Arrays.asList(1, 7, 3, 8, 2, 4, 9);
//        Integer reduce = numbers.stream().reduce(0, Integer::sum);

//        numbers.stream().reduce(Integer::max)
//        numbers.stream().sorted(Integer::compareTo).forEach(System.out::println);
//
//        Stream.of("silently", "9527", "silently9527.cn")
//                .forEach(System.out::println);
//
//        int[] nums = {3, 5, 2, 7, 8, 9};
//        Arrays.stream(nums).sorted().forEach(System.out::println);
//
//        Files.lines(Paths.get("/Users/huaan9527/Desktop/data.txt"))
//                .forEach(System.out::println);

//        Optional<Integer> min = numbers.stream().collect(Collectors.minBy(Integer::compareTo));
//        Optional<Integer> max = numbers.stream().collect(Collectors.maxBy(Integer::compareTo));
//        System.out.println(min.get());
//        System.out.println(max.get());


//        List<Integer> result = new ArrayList<>();
//        for (Integer num : data) {
//            if (num > 5) {
//                result.add(num);
//            }
//        }
//        return result;
//
//        List<Integer> data = Stream.of(1, 7, 3, 8, 2, 4, 9, 7, 9)
//                .filter(num -> num > 5)
//                .skip(1)
//                .limit(2)
//                .collect(toList());
//        System.out.println(data);

        List<Customer> allCustomers = Arrays.asList(
                new Customer("silently9525", 10, "四川"),
                new Customer("silently9526", 30, "北京"),
                new Customer("silently9527", 20, "深圳"),
                new Customer("silently9517", 40, "深圳"));

        Map<String, Map<Integer, List<Customer>>> groups = allCustomers.stream()
                .collect(groupingBy(Customer::getArea, groupingBy(Customer::getAge)));

        Map<String, Long> collect = allCustomers.stream().collect(groupingBy(Customer::getArea, counting()));

        Map<String, Optional<Customer>> optionalMap = allCustomers.stream()
                .collect(groupingBy(Customer::getArea, maxBy(Comparator.comparing(Customer::getAge))));

        System.out.println(optionalMap);

//        Optional<Customer> minAgeCustomer = allCustomers.stream().collect(minBy(Comparator.comparing(Customer::getAge)));
//        Optional<Customer> maxAgeCustomer = allCustomers.stream().collect(maxBy(Comparator.comparing(Customer::getAge)));
//
//        Double avgAge = allCustomers.stream().collect(averagingInt(Customer::getAge));
//
//        allCustomers.stream().map(Customer::getName).collect(joining(","));
//
//        Map<Integer, List<Customer>> groupByAge = allCustomers.stream().collect(groupingBy(Customer::getAge));


//        Optional<Customer> optional = allCustomers.stream()
//                .filter(customer -> customer.getAge() > 20)
//                .findAny();
//        allCustomers.stream()
//                .filter(customer -> customer.getAge() > 20)
//                .map(customer -> customer.getName().split(""))
//                .flatMap(Arrays::stream)
//                .forEach(System.out::println);

//        if (allCustomers.stream().anyMatch(customer -> "silently9527".equals(customer.getName()))) {
//            System.out.println("存在用户silently9527");
//        }
//
//        if (allCustomers.stream().noneMatch(customer -> customer.getAge() < 20)) {
//            System.out.println("所有用户年龄都大于20");
//        }


    }

}
