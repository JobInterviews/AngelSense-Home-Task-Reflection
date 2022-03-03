package com.angelsense;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class Utility {

    private static final String LINE = "--------------------";
    private Set<Object> visited;

    public void analyze(Object obj) throws IllegalArgumentException, IllegalAccessException {
        visited = new HashSet<>();
        analyze(obj, 0);
        visited.clear();
    }

    private void analyze(Object obj, int level) throws IllegalArgumentException, IllegalAccessException {
//        if(visited.contains(obj)){
//            printRepeatedClass(obj, level);
//            return;
//        }

        visited.add(obj);
        Class<?> objClass = obj.getClass();

        printClassTitle(obj, level);
        Field[] fields = objClass.getDeclaredFields();

        for(Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object fieldValue = field.get(obj);

            if(isString(field) || isPrimitive(field)){
                printField(fieldName, fieldValue+"", level);
            }else if(isCollection(field)) {
                analyzeCollection(level, fieldName, fieldValue);
            }else if(isArray(field)) {
                analyzeArray(level, fieldName, fieldValue);
            }else { //struct
                analyze(fieldValue, level++);
            }
        }
    }

    private void analyzeCollection(int level, String fieldName, Object fieldValue) throws IllegalAccessException {
        if(fieldValue == null)
            printField(fieldName, null, level);
        else {
            System.out.println(level+"_"+leftPadding(fieldName + " = ", level));
            analyzeCollection(fieldValue, level);
        }
    }

    private void analyzeArray(int level, String fieldName, Object fieldValue) throws IllegalAccessException {
        if(fieldValue == null)
            printField(fieldName, null, level);
        else if(isPrimitiveArray(fieldValue))
            printField(fieldName, getArrayValue(fieldValue), level);
        else {
            System.out.println(leftPadding(level+"_"+fieldName + " = ", level));
            analyzeNonPrimitiveArray(fieldValue, level);
        }
    }

    private void printField(String fieldName, String fieldValue, int level) {
        System.out.println(leftPadding(level+"_"+fieldName + " = " + fieldValue, level));
    }

    private String leftPadding(String input, int length) {
        return "\t".repeat(length) + input;
    }

    private String getArrayValue(Object array) {
        return Arrays.toString(toArray(array));
    }

    private boolean isPrimitiveArray(Object array) {
        return array.getClass().getComponentType().isPrimitive();
    }

    @SuppressWarnings("unchecked")
    private void analyzeCollection(Object obj, int level) throws IllegalAccessException {
        level++;
        for (Object o : (Collection<Object>) obj)
            if(o != null)
                analyze(o, level);
    }

    private void analyzeNonPrimitiveArray(Object array, int level) throws IllegalAccessException {
        level++;

        for (Object o : (Object[]) array)
            if(o != null)
                analyze(o, level);
    }

    private boolean isArray(Field field) {
        return field.getType().isArray();
    }

    private void printClassTitle(Object obj, int level) {
        System.out.println("\n" + leftPadding("Object of Class " + getName(obj), level));
        System.out.println(leftPadding(LINE, level));
    }

    private void printRepeatedClass(Object obj, int level) {
        System.out.println("\n" + leftPadding("Object of Class " + getName(obj), level) + " was printed before");
        System.out.println(leftPadding(LINE, level));
    }

    private String getName(Object obj) {
        return obj.getClass().getSimpleName();
    }

    private boolean isString(Field field){
        return String.class.isAssignableFrom(field.getType());
    }

    private boolean isCollection(Field field){
        return Collection.class.isAssignableFrom(field.getType());
    }

    private boolean isPrimitive(Field field){
        return field.getType().isPrimitive();
    }

    private Object[] toArray(Object obj) {
        int len = Array.getLength(obj);
        Object[] res = new Object[len];
        for (int i = 0; i < len; i++)
            res[i] = Array.get(obj, i);

        return res;
    }

    public static void main(String[] args) {
        Name n = new Name();
        n.firstName = "John";
        n.lastName = "Doe";

        Name n1 = new Name();
        n1.firstName = "John1";
        n1.lastName = "Doe1";

        Name n2 = new Name();
        n2.firstName = "John2";
        n2.lastName = "Doe2";

        Name n3 = new Name();
        n3.firstName = "John3";
        n3.lastName = "Doe3";

        n.children = List.of(n1, n2);
        n.arrayOfThings = new Object[]{n1, n2};
        n1.arrayOfThings = new Object[]{n3};
       // n.arrayOfThings = new Object[]{"ssss", "ddd"};

       // n.arrayOfInts = new int[]{1,2};

        Person p = new Person();
        p.age = 55;
        p.name = n;


        Utility utility  = new Utility();

        try {
            utility.analyze(p);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static class Name {
        public String firstName;
        public String lastName;
        public List<?> children;
        public Object[] arrayOfThings;
        public int[] arrayOfInts;
    }
    
    private static class Person {
        public int age;
        public Name name;

        //private List childern = new ArrayList<>();
    }
}
